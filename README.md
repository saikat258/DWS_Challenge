# DWS_Challenge
This repository has a code that is a sample for amount transfer between account ids. 
This repo has been developed as a part of coding round challenge.

Here I have used a lock per account, but to avoid deadlock I have actually acquired locks 
in the same order always by comparing the account ids.