class initialization
displayOnTranscript: entries
	WorldState addDeferredUIMessage: [
		entries do: [:entry | Transcript show: entry value; cr]]