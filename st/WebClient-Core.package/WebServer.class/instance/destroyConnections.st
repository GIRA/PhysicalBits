initialize
destroyConnections
	"Destroy all current connections."

	mutex critical:[
		connections do:[:p| p terminate].
		connections removeAll.
	].
