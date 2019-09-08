reporting
updateReportingFor: anUziProgram
	| ast |
	self sendGlobalsReporting. "Currently enabled globals should remain enabled"
	ast := anUziProgram node.
	ast ifNil: [^ self].
	
	"All pins and globals referenced in the program must be enabled as well"
	ast pins do: [:pin | self setPin: pin name report: true].
	ast globals do: [:global | self setGlobal: global name report: true]