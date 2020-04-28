
var simCtor = require('../ide/simulator');

function initializeSimulator()
{
    return simCtor();
}

var sim = initializeSimulator();

console.log(sim.startDate);
