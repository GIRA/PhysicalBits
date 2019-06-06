initialize-release
update: outputMsg
	clients do: [:client |
		client at: #changed put: true.
		(client at: #output) nextPutAll: outputMsg.
		(client at: #semaphore) signal]