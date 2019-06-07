initialize-release
updateOutput: logEntries
	clients do: [:client |
		client at: #changed put: true.
		(client at: #output) enqueueAll: logEntries.
		(client at: #semaphore) signal]