initialize-release
update
	clients do: [:client |
		client at: #changed put: true.
		(client at: #semaphore) signal]