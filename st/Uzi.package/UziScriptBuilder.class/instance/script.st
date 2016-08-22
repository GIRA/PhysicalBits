building
script	
	instructions withIndexDo: [:instruction :index |
		instruction updateLabelArgument: (self labelsFrom: index)].
	^ UziScript
		name: scriptName
		ticking: ticking
		delay: delay
		instructions: instructions 