# SOLUTIONS

## Level 1:

Multiply the four numbers in the hints together.
Multiplication is hinted to by the "\*=" symbol in the platforms.

---

## Level 2:

Reverse the digits in the number, convert to hexadecimal, then convert every two
characters to its corresponding ASCII character.

---

## Level 3:

Caesar shift by a shift of 11 towards the right.

---

## Level 4:

Substitution cipher using the reverse alphabet (ZXYW...CBA) as the subtitution
alphabet.

That is, A maps to Z, B maps to Y...Y maps to B, and Z maps to A.

---

## Level 5:

Playfair cipher with the key PLAYFIRNOCHETGBDJKMQSUWXZ.

---

## Level 6:

Vigenere cipher with key "crescendo".

---

## Level 7:

Decode from base-26 to base-10. a = 0, b = 1, ... y = 24, z = 25.

---

## Level 8:

Convert "D0_3><C1u51\/30r" to hexadecimal then to decimal, then XOR with it.

---

## Level 9:

Rivest-Shamir-Adleman (RSA) is secure when N is very large, unlike this example.

Factor N to get P and Q.
Calculate the decryption key, D using P, Q, and E.
Decrypt using D.

Convert to hexadecimal.

---

## Level 10:

The 32-length hexadecimal password from the previous level should have been a clue
that this is a hash.

Look up the MD5-hash to get the final plaintext.
