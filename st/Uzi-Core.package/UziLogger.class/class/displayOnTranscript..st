class initialization
displayOnTranscript: entries
	WorldState addDeferredUIMessage: [
		entries do: [:entry || msg args |
			msg := self asFormatString: entry value first.
			args := entry value second.
			Transcript show: (msg format: args); cr]]