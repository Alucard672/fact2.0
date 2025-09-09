package com.garment.common.interceptor;

import com.baomidou.mybatisplus.core.toolkit.PluginUtils;
import com.baomidou.mybatisplus.extension.parser.JsqlParserSupport;
import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import com.garment.common.context.TenantContext;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectBody;
import net.sf.jsqlparser.statement.select.SetOperationList;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

/**
 * 多租户数据隔离拦截器
 *
 * @author garment
 */
@Slf4j
public class TenantInterceptor extends JsqlParserSupport implements InnerInterceptor {
    
    private static final Logger log = LoggerFactory.getLogger(TenantInterceptor.class);

    /**
     * 需要进行租户隔离的表
     */
    private static final List<String> TENANT_TABLES = Arrays.asList(
        "styles", "processes", "process_prices", "cut_orders", "bundles", 
        "production_flows", "piecework_records", "payroll_periods", 
        "repair_records", "print_tasks", "workshops", "customers"
    );

    @Override
    public void beforeQuery(Executor executor, MappedStatement ms, Object parameter, 
                           RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) throws SQLException {
        
        Long tenantId = TenantContext.getCurrentTenantId();
        if (tenantId == null) {
            // 如果没有租户上下文，跳过处理（比如系统管理员操作）
            return;
        }

        PluginUtils.MPBoundSql mpBoundSql = PluginUtils.mpBoundSql(boundSql);
        mpBoundSql.sql(this.parserSingle(mpBoundSql.sql(), tenantId));
    }

    @Override
    protected void processSelect(Select select, int index, String sql, Object obj) {
        Long tenantId = (Long) obj;
        SelectBody selectBody = select.getSelectBody();
        
        if (selectBody instanceof PlainSelect) {
            this.processPlainSelect((PlainSelect) selectBody, tenantId);
        } else if (selectBody instanceof SetOperationList) {
            SetOperationList setOperationList = (SetOperationList) selectBody;
            List<SelectBody> selectBodyList = setOperationList.getSelects();
            selectBodyList.forEach(s -> {
                if (s instanceof PlainSelect) {
                    this.processPlainSelect((PlainSelect) s, tenantId);
                }
            });
        }
    }

    /**
     * 处理PlainSelect
     */
    protected void processPlainSelect(PlainSelect plainSelect, Long tenantId) {
        Expression where = plainSelect.getWhere();
        Expression tenantCondition = this.buildTenantCondition(plainSelect, tenantId);
        
        if (tenantCondition != null) {
            if (where != null) {
                plainSelect.setWhere(new AndExpression(where, tenantCondition));
            } else {
                plainSelect.setWhere(tenantCondition);
            }
        }
    }

    /**
     * 构建租户条件
     */
    protected Expression buildTenantCondition(PlainSelect plainSelect, Long tenantId) {
        try {
            String tableName = plainSelect.getFromItem().toString();
            
            // 移除表别名
            if (tableName.contains(" ")) {
                tableName = tableName.split(" ")[0];
            }
            
            // 移除数据库名和引号
            if (tableName.contains(".")) {
                tableName = tableName.substring(tableName.lastIndexOf(".") + 1);
            }
            tableName = tableName.replace("`", "").replace("\"", "");
            
            // 检查是否需要添加租户隔离
            if (TENANT_TABLES.contains(tableName)) {
                EqualsTo equalsTo = new EqualsTo();
                equalsTo.setLeftExpression(new Column("tenant_id"));
                equalsTo.setRightExpression(new LongValue(tenantId));
                
                log.debug("添加租户隔离条件: {} tenant_id = {}", tableName, tenantId);
                return equalsTo;
            }
        } catch (Exception e) {
            log.warn("构建租户条件失败: {}", e.getMessage());
        }
        
        return null;
    }
}