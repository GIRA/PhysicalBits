benchmarks
requestBenchmark: persistent
	"Runs a WebClient/WebServer benchmark to measure request handling speed.
	Uses a persistent connection if requested."

	"
		WebServer requestBenchmark: false.
		WebServer requestBenchmark: true.
		MessageTally spyOn:[WebServer requestBenchmark: true]
	"

	| port |
	port := 8888.
	WebServer reset default listenOn: port.
	WebServer default addService: '/benchmark' action:[:req|
		req send200Response: ''.
	].
	^[WebClient 
		requestBenchmark: 'http://localhost:', port, '/benchmark'
		persistent: persistent] ensure:[WebServer reset].
