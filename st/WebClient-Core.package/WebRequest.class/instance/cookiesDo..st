cookies
cookiesDo: aBlock
	"Parse the cookie headers and evaluate aBlock with name / value pairs"

	self headersAt: 'Cookie' do:[:hdr|
		(hdr findTokens: ';,') do:[:token|
			aBlock 
				value: (token copyUpTo: $=) withBlanksTrimmed 
				value: (token copyAfter: $=) withBlanksTrimmed]].