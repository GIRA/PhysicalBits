var assert = require('assert');
var simCtor = require('../ide/simulator');

function initializeSimulator()
{
    return simCtor();
}

it('sanity-check', function () {
  assert.equal(0, 0);  
});
