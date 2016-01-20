src :: FromDevice()
dst :: ToDevice()

q :: ARPQuerier(10.0.0.1, aa:aa:aa:aa:aa:aa)
r :: ARPResponder(10.0.0.2/24, aa:aa:aa:aa:aa:ab)
c :: ARPClassifier()

src -> q -> r -> dst
r[1] -> [1]q
q[1] -> dst

