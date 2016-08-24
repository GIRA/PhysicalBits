as yet unclassified
openPort: host
	| parts |
	[parts := host subStrings: ':'.
	socket := Socket newTCP connectToHostNamed: parts first port: parts second asInteger]
		on: Error do: [^ nil]