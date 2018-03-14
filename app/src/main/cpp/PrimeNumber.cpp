//
// Created by m1035364 on 15/3/18.
//

#include "PrimeNumber.h"

PrimeNumber::PrimeNumber(int x) {
    PrimeNumber::number=x;
}

bool PrimeNumber::isPrime() {
    bool isPrime=true;
    for (int i = 2; i <number/2 ; i++) {
        if(number%i==0) return false;
        else isPrime= true;
    }
    return isPrime;
}