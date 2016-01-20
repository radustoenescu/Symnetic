src :: FromDevice()
dst :: ToDevice()

src -> checker :: CheckARPHeader() -> dst
checker[1] -> dst
