parts bin
descriptionForPartsBin
	"Answer a description for use in parts bins"
	^ self
		partName: 'Uzi Protocol' translatedNoop
		categories: (Project current isPEProject
						ifTrue: [{'Physical Etoys' translatedNoop}]
						ifFalse: [#()])
		documentation: 'An object that lets you communicate with an arduino board using the Uzi protocol' translatedNoop