initialize
initialize

	super initialize.
	closing := false.
	masking := true. " 	http://tools.ietf.org/html/draft-ietf-hybi-thewebsocketprotocol-07#section-4.1 says:
   ''Frames sent from the client to the server are masked to
   avoid confusing network intermediaries, such as intercepting proxies.''"