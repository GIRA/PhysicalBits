building
buildViewerButton
	^ (ThreePhaseButtonMorph labelSymbol: 'Halo-View')
		target: self;
		actionSelector: #beViewed