const crypto = require('crypto');

function b64url(input) {
  return Buffer.from(input)
    .toString('base64')
    .replace(/=/g, '')
    .replace(/\+/g, '-')
    .replace(/\//g, '_');
}

const header = { alg: 'HS256', typ: 'JWT' };
const now = Math.floor(Date.now() / 1000);
const payload = {
  sub: 'admin',
  iat: now,
  exp: now + 1800,
  userid: 1,
  tenantId: 1,
  role: 'ADMIN',
};

const secret = 'garment-production-jwt-secret-key-2024';

const eh = b64url(JSON.stringify(header));
const ep = b64url(JSON.stringify(payload));
const data = `${eh}.${ep}`;
const sig = crypto
  .createHmac('sha256', secret)
  .update(data)
  .digest('base64')
  .replace(/=/g, '')
  .replace(/\+/g, '-')
  .replace(/\//g, '_');

console.log(`${data}.${sig}`);