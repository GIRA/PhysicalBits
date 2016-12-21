benchmark
requestBenchmark: url persistent: aBool
	"Run a WebClient benchmark to measure request handling speed.
	Try using a persistent connection if requested.

	1) Launch WebServer  (or Seaside, or whatever):
	
		(WebServer reset default)
			listenOn: 8888;
			addService: '/' action:[:r| r send200Response: ''].

	2) Run the benchmark:

		MessageTally spyOn:[
			WebClient requestBenchmark: 'http://localhost:8888/' persistent: true.
		]
	"

	| time resp startTime endTime count client |
	count := 0.
	client := WebClient new.
	startTime := Time millisecondClockValue.
	[endTime := Time millisecondClockValue.
	(time := Time milliseconds: endTime since: startTime) < 5000] whileTrue:[
		resp := client httpGet: url.
		resp isSuccess ifFalse:[self error: resp status].
		resp content.
		aBool ifFalse:[resp close].
		count := count + 1.
	].
	^(count  * 1000 // time) asStringWithCommas, ' requests/sec'
