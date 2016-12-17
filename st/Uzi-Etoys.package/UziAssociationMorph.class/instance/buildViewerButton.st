initialize
buildViewerButton
	| form pressedForm |
	form := ScriptingSystem formAtKey: #'Halo-View'.
	pressedForm := ScriptingSystem formAtKey: #'Halo-ViewPressed'.
	^ (ThreePhaseButtonMorph new
		image: form;
		offImage: form;
		pressedImage: (pressedForm ifNil: [form]))
		target: self;
		actionSelector: #beViewed