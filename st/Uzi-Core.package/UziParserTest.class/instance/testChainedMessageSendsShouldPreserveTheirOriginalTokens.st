tests
testChainedMessageSendsShouldPreserveTheirOriginalTokens
	| ast pp body msg1 msg2 |
	pp := parser.
	ast := pp parse: '@foo [D13 read on]'.
	body := ast scripts first body.
	msg1 := body statements first.
	msg2 := msg1 receiver.
	self assert: msg1 token notNil.
	self assert: msg2 token notNil
