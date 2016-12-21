benchmarks
benchmark
	"Runs a WebClient/WebServer streaming benchmark to measure throughput"

	"
		WebServer benchmark
		MessageTally spyOn:[WebServer benchmark]
	"

	| amount time resp port |
	port := 8888.
	amount := 128 * 1024 * 1024.
	WebServer reset default listenOn: port.
	WebServer default addService: '/streaming' action:[:req|
		req stream200Response: NullStream new size: amount.
	].
	time := [
		resp := WebClient httpGet: 'http://localhost:', port, '/streaming'.
		resp isSuccess ifFalse:[self error: resp status].
		resp streamTo: NullStream new size: resp contentLength progress: nil.
		resp close.
	] timeToRun.
	WebServer reset.
	^(amount  * 1000 // time) asStringWithCommas, ' bytes/sec'
