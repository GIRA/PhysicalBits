initialize-release
updateOutput: logEntries
	clients do: [:client |
		client at: #changed put: true.
		client at: #output put: logEntries.
		(client at: #semaphore) signal]