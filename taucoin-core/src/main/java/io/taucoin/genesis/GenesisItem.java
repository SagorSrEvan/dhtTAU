/**
Copyright 2020 taucoin developer

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files
(the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge,
publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do
so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED
TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT
SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE
OR OTHER DEALINGS IN THE SOFTWARE.
*/
package io.taucoin.genesis;

import java.math.BigInteger;

import io.taucoin.param.ChainParam;
import io.taucoin.util.*;

public class GenesisItem {
    private BigInteger balance;
    private BigInteger power;
    private byte[] rlpEncoded = null;
    private boolean isParse;

    /**
     * genesis account status that community creator can set.
     * @param balance
     * @param power
     */
    public GenesisItem(BigInteger balance,BigInteger power) {
        this.balance = balance;
        this.power = power;
        isParse = true;
    }

    /**
     * genesis default account power to ensure block chain smoothly.
     * @param balance
     */
    public GenesisItem(BigInteger balance){
        this.balance = balance;
        this.power  = ChainParam.DefaultGeneisisPower;
        isParse = true;
    }

    public GenesisItem(byte[] rlpEncoded){
        this.rlpEncoded = rlpEncoded;
        this.isParse = false;
    }

    /**
     * encode genesis item.
     * @return
     */
    public byte[] getEncoded(){
        if(rlpEncoded == null){
            byte[] balance = RLP.encodeElement(this.balance.toByteArray());
            byte[] power = RLP.encodeElement(this.power.toByteArray());
            rlpEncoded = RLP.encodeList(balance,power);
        }
        return rlpEncoded;
    }

    public BigInteger getBalance() {
        if(!isParse) parseRLP();
        return balance;
    }

    public BigInteger getPower(){
        if(!isParse) parseRLP();
        return power;
    }

    private void parseRLP(){
        if(isParse){
            return;
        }else {
            RLPList items = RLP.decode2(this.rlpEncoded);
            RLPList item = (RLPList)items.get(0);
            this.balance = new BigInteger(item.get(0).getRLPData());
            byte[] powers = item.get(1).getRLPData()==null? BigInteger.ZERO.toByteArray():item.get(1).getRLPData();
            this.power = new BigInteger(powers);
            isParse = true;
        }
    }
}
