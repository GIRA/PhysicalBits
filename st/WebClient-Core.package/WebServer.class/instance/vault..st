accessing
vault: anObject
	"The vault used for storing password hashes.

	Clients can provide their own storage mechanism. 
	Subclasses can also override the lookup and authentication mechanism."

	vault := anObject