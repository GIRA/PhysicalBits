viewer categories
additionsToViewerCategories
	^ #(
	(#'uzi' (
#(command connect 'Connects this device' )
#(command disconnect 'Disconnects this device' )
#(slot isConnected 'Shows if the device is connected' Boolean readOnly Player getIsConnected Player unused )
#(slot portName 'Name of the OS port in which the device is connected' PEPort readWrite Player getPortName Player setPortName: )
#(command run: '' String)
#(command installCurrentProgram '' )
))
)