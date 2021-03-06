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
import com.frostwire.jlibtorrent.Ed25519;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.taucoin.config.ChainConfig;
import io.taucoin.param.ChainParam;
import io.taucoin.types.Block;
import io.taucoin.types.GenesisMsg;
import io.taucoin.util.ByteUtil;

public class GenesisTest {
    private static final Logger log = LoggerFactory.getLogger("genesisTest");
    private static final ChainConfig cf  = ChainConfig.NewTauChainConfig();
    private static final String minerpubkey = "cedf8491b85d3b536b93831458eaccd8518191a99abb0f0e4abc56fd51739617";
    private static final String minerprikey = "301f779c9c258f3e7e734d765251a6f366f5194f0133c946245ad73a5bfae147a5d0d0f6d3816097aea7f98b41a4081fe072df06727725f2ed5cf00e355b0c69";
    private static final String sig = "ee88e46bc571d98db8f0c57ea33bee489609791edf247f43950af2d8cfe5fd4665af4b1f5ec6e71b33e4e076f508a2e95a2e259a9400570f722d11411499afd614";
    private static final String generationSig = "4a9b7fa2a4f5cfba2ac1f159c26d1e93a771024e96f28ca22d667adf98947f5f";
    private static final String communityMsg = "f901789392636f6d6d756e69747920636861696e2e2e2eb857b855636137326661396331303035333134326331333032343235343132643666366163316230336562646332636263356663373637366431633331666464623665313a6339383730316336626635323633343030303830b857b855633739386663393161343033626366633664623230666662336331643966353935326566636238653039623430393733333863623739373439393361323130633a6339383730316336626635323633343030303830b857b855313864633465653933313637373066323631636131376364336262663331393330316139663039333062653361626366666262373365373361353064396662313a6339383730316336626635323633343030303830b857b855303930313339623430636466626334653830336630613532393366333030356161623531333265303838333231626166343463323736616433636634616264613a6339383730316336626635323633343030303830";
    private static final GenesisMsg msg = new GenesisMsg(ByteUtil.toByte(communityMsg));
    private static final ChainConfig ccf = ChainConfig.NewChainConfig((byte)1,"hello world",600,minerpubkey,generationSig,msg);
    private static final ChainConfig ccf1 = ChainConfig.NewChainConfig((byte)1,"beijing",600,minerpubkey,generationSig,msg);
    private static final String genesis = "f902c401b4544155636f696e233330302333343332333933343636333036333339333033383331363136353338333633343331333736343335880000000000e4e1c088000000000000000080808721d0369d03697880a09f0c546cceb1932f01d3fe99029038663fe75f2e65377f88093ae2c30ee31f27f9020701b4544155636f696e23333030233334333233393334363633303633333933303338333136313635333833363334333133373634333588000000005f1578f5880000000000000000a04294f0c9081ae86417d547774fc4c61c29468ea6298a1165644d323c45fb2e6d880000000000000000f9019180b9018df9018a9594636f6d6d756e69747920636861696e2e2e2e7575b85bb859636137326661396331303035333134326331333032343235343132643666366163316230336562646332636263356663373637366431633331666464623665313a636238366533356661393331613030303833303139616130b85bb859633739386663393161343033626366633664623230666662336331643966353935326566636238653039623430393733333863623739373439393361323130633a636238366533356661393331613030303833303139616130b85bb859313864633465653933313637373066323631636131376364336262663331393330316139663039333062653361626366666262373365373361353064396662313a636238366533356661393331613030303833303139616130b85bb859303930313339623430636466626334653830336630613532393366333030356161623531333265303838333231626166343463323736616433636634616264613a6362383665333566613933316130303038333031396161308088000000000000000088000000000000000088000000000000000088000000000000000080a04294f0c9081ae86417d547774fc4c61c29468ea6298a1165644d323c45fb2e6d";
    private static final String cgenesis = "f902bc01b83868656c6c6f20776f726c6423363030233633363536343636333833343339333136323338333536343333363233353333333636323339333388000000005f15791288000000000000000080808721d0369d03697880a04a9b7fa2a4f5cfba2ac1f159c26d1e93a771024e96f28ca22d667adf98947f5ff901fa01b83868656c6c6f20776f726c6423363030233633363536343636333833343339333136323338333536343333363233353333333636323339333388000000005f157912880000000000000000a0cedf8491b85d3b536b93831458eaccd8518191a99abb0f0e4abc56fd51739617880000000000000000f9017f80b9017bf901789392636f6d6d756e69747920636861696e2e2e2eb857b855636137326661396331303035333134326331333032343235343132643666366163316230336562646332636263356663373637366431633331666464623665313a6339383730316336626635323633343030303830b857b855633739386663393161343033626366633664623230666662336331643966353935326566636238653039623430393733333863623739373439393361323130633a6339383730316336626635323633343030303830b857b855313864633465653933313637373066323631636131376364336262663331393330316139663039333062653361626366666262373365373361353064396662313a6339383730316336626635323633343030303830b857b855303930313339623430636466626334653830336630613532393366333030356161623531333265303838333231626166343463323736616433636634616264613a63393837303163366266353236333430303038308088000000000000000088000000000000000088000000000000000088000000000000000080a0cedf8491b85d3b536b93831458eaccd8518191a99abb0f0e4abc56fd51739617";
    @Test
    public void createTauGenesis(){
        Block genesis = new Block(cf);
        log.debug("is genesis block validate??: "+genesis.isBlockParamValidate());
//        byte[] prikey = ByteUtil.toByte("Known by tau genesis");
//        byte[] sig = genesis.signBlock(prikey);
        log.debug("signature: "+ChainParam.TauGenesisSignature);
        genesis.setSignature(ByteUtil.toByte(ChainParam.TauGenesisSignature));
//        log.debug(ByteUtil.toHexString(genesis.getSignature()));
        log.debug("genesis: "+ByteUtil.toHexString(genesis.getEncoded()));
        log.debug("tau genesis block: "+genesis.toString());
        if(Ed25519.verify(genesis.getSignature(),genesis.getBlockSigMsg(),ByteUtil.toByte(ChainParam.TauGenesisMinerPubkey))){
            log.debug("verify passed....");
        }
        log.debug("signature verify ?: "+genesis.verifyBlockSig());
    }

    @Test
    public void createCommunityGenesis(){
        Block genesis = new Block(ccf);

        log.debug("is genesis block validate?: "+genesis.isBlockParamValidate());
        String sig = ByteUtil.toHexString(genesis.signBlock(ByteUtil.toByte(minerprikey)));
        log.debug("signature: "+sig);
        String str = ByteUtil.toHexString(genesis.getEncoded());
        log.debug(str);
        log.debug("is genesis block validate??: "+genesis.isBlockParamValidate());
        log.debug("size is: "+str.length()/2 +" bytes");
        log.debug("is signed genesis: "+genesis.verifyBlockSig());
        log.debug("genesis hash is: "+ByteUtil.toHexString(genesis.getBlockHash()));

        Block genesis1 = new Block(ccf1);
        String str1 = ByteUtil.toHexString(genesis1.getEncoded());
        log.debug(str1);
        log.debug("size is: "+str1.length()/2 +" bytes");
    }

    @Test
    public void decodeTauGenesis(){
        Block genesis = new Block(ByteUtil.toByte(this.genesis));
        String ID = new String(genesis.getChainID());
        log.debug("chainid: "+ID);
    }

    @Test
    public void decodeCommunityGenesis(){
        Block genesis = new Block(ByteUtil.toByte(this.cgenesis));
        String ID = new String(genesis.getChainID());
        log.debug("chainid: "+ID);
    }

}
