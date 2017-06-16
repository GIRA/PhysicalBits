private
name: nameSymbol code: codeInteger stackTransition: stackTransition
	"Private! You should use #named: instead"
	^ self basicNew 
		initializeWithName: nameSymbol 
		code: codeInteger
		stackTransition: stackTransition