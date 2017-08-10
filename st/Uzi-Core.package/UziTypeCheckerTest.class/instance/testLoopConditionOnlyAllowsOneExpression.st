tests
testLoopConditionOnlyAllowsOneExpression
	"self assertInvalid: 'task foo() { until (1; read(D13)); }'"