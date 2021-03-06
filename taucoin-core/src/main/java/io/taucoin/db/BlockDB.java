package io.taucoin.db;

import io.taucoin.param.ChainParam;
import io.taucoin.types.Block;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BlockDB implements BlockStore {
    private static final Logger logger = LoggerFactory.getLogger("BlockDB");

    private KeyValueDataBase db;

    public BlockDB(KeyValueDataBase db) {
        this.db = db;
    }

    /**
     * open db
     * @param path database path which can be accessed
     * @throws Exception
     */
    public void open(String path) throws Exception {
        db.open(path);
    }

    /**
     * close db
     */
    public void close() {
        db.close();
    }

    /**
     * get block by hash
     * @param chainID
     * @param hash
     * @return
     * @throws Exception
     */
    @Override
    public Block getBlockByHash(byte[] chainID, byte[] hash) throws Exception {
        byte[] rlp = db.get(PrefixKey.blockKey(chainID, hash));
        if (null != rlp) {
            return new Block(rlp);
        }
        logger.info("ChainID[{}]:Cannot find block by hash:{}", chainID.toString(), Hex.toHexString(hash));
        return null;
    }

    /**
     * get block info
     * @param chainID
     * @param number
     * @param hash
     * @return
     * @throws Exception
     */
    private BlockInfo getBlockInfo(byte[] chainID, long number, byte[] hash) throws Exception {
        byte[] rlp = db.get(PrefixKey.blockInfoKey(chainID, number));
        if (null == rlp) {
            logger.info("ChainID[{}]:There is no block in this height:{}", chainID.toString(), number);
            return null;
        }
        BlockInfos blockInfos = new BlockInfos(rlp);
        List<BlockInfo> list = blockInfos.getBlockInfoList();
        for (BlockInfo blockInfo: list) {
            if (Arrays.equals(blockInfo.getHash(), hash)) {
                return blockInfo;
            }
        }

        return null;
    }

    /**
     * get block info by hash
     *
     * @param chainID
     * @param hash
     * @return
     * @throws Exception
     */
    @Override
    public BlockInfo getBlockInfoByHash(byte[] chainID, byte[] hash) throws Exception {
        byte[] rlp = db.get(PrefixKey.blockKey(chainID, hash));
        if (null != rlp) {
            Block block = new Block(rlp);
            return getBlockInfo(chainID, block.getBlockNum(), hash);
        }

        logger.info("ChainID[{}]:Cannot find block info by hash:{}", chainID.toString(), Hex.toHexString(hash));
        return null;
    }

    /**
     * get main chain block by number
     * @param number
     * @return
     * @throws Exception
     */
    @Override
    public Block getMainChainBlockByNumber(byte[] chainID, long number) throws Exception {
        byte[] rlp = db.get(PrefixKey.blockInfoKey(chainID, number));
        if (null == rlp) {
            logger.info("ChainID[{}]:There is no block in this height:{}", chainID.toString(), number);
            return null;
        }
        BlockInfos blockInfos = new BlockInfos(rlp);
        List<BlockInfo> list = blockInfos.getBlockInfoList();
        for (BlockInfo blockInfo: list) {
            if (blockInfo.isMainChain()) {
                getBlockByHash(chainID, blockInfo.getHash());
            }
        }
        logger.info("ChainID[{}]:There is no main chain block in this height:{}", chainID.toString(), number);
        return null;
    }

    /**
     * get main chain block hash by number
     *
     * @param chainID chain ID
     * @param number  block number
     * @return block hash
     * @throws Exception
     */
    @Override
    public byte[] getMainChainBlockHashByNumber(byte[] chainID, long number) throws Exception {
        byte[] rlp = db.get(PrefixKey.blockInfoKey(chainID, number));
        if (null == rlp) {
            logger.info("ChainID[{}]:There is no block in this height:{}", chainID.toString(), number);
            return null;
        }
        BlockInfos blockInfos = new BlockInfos(rlp);
        List<BlockInfo> list = blockInfos.getBlockInfoList();
        for (BlockInfo blockInfo: list) {
            if (blockInfo.isMainChain()) {
                return blockInfo.getHash();
            }
        }
        logger.info("ChainID[{}]:There is no main chain block in this height:{}", chainID.toString(), number);
        return null;
    }

    /**
     * save block info in db
     * @param block
     * @param isMainChain
     * @throws Exception
     */
    public void saveBlockInfo(Block block, boolean isMainChain) throws Exception {
        BlockInfos blockInfos;
        // if saved in the same height
        byte[] rlp = db.get(PrefixKey.blockInfoKey(block.getChainID(), block.getBlockNum()));
        if (null == rlp) {
            blockInfos = new BlockInfos();
        } else {
            blockInfos = new BlockInfos(rlp);
        }
        blockInfos.putBlock(block, isMainChain);

        db.put(PrefixKey.blockInfoKey(block.getChainID(), block.getBlockNum()), blockInfos.getEncoded());
    }

    /**
     * save block
     * @param block
     * @throws Exception
     */
    @Override
    public void saveBlock(Block block, boolean isMainChain) throws Exception {
        // save block
        db.put(PrefixKey.blockKey(block.getChainID(), block.getBlockHash()), block.getEncoded());
        // save block info
        saveBlockInfo(block, isMainChain);
        // delete fork chain blocks out of 3 * mutable range, when save main chain block
        if (isMainChain && block.getBlockNum() > ChainParam.WARNING_RANGE) {
            long number = block.getBlockNum() - ChainParam.WARNING_RANGE;
            logger.info("ChainID[{}]: Delete fork chain block in height:{}",
                    block.getChainID().toString(), number);
            delForkChainBlockByNumber(block.getChainID(), number);
        }
    }

    /**
     * delete fork chain block and block info
     * @param chainID
     * @param number
     * @throws Exception
     */
    public void delForkChainBlockByNumber(byte[] chainID, long number) throws Exception {
        byte[] rlp = db.get(PrefixKey.blockInfoKey(chainID, number));
        if (null == rlp) {
            logger.info("ChainID[{}]: There is no block in this height:{}", chainID.toString(), number);
            return;
        }
        BlockInfos blockInfos = new BlockInfos(rlp);
        List<BlockInfo> list = blockInfos.getBlockInfoList();
        for (int i = list.size() - 1; i >= 0; i--) {
            if (!list.get(i).isMainChain()) {
                // delete non-main chain block
                db.delete(PrefixKey.blockKey(chainID, list.get(i).getHash()));
                list.remove(i);
            }
        }
        // save main chain block info
        db.put(PrefixKey.blockInfoKey(chainID, number), blockInfos.getEncoded());
    }

    /**
     * get all blocks of a chain, whether it is a block on the main chain or not
     * @param chainID
     * @return
     * @throws Exception
     */
    @Override
    public Set<Block> getChainAllBlocks(byte[] chainID) throws Exception {
        Set<byte[]> blocks = db.retrieveKeysWithPrefix(PrefixKey.blockPrefix(chainID));
        if (null == blocks) {
            logger.info("ChainID[{}]: Cannot find any block", chainID.toString());
            return null;
        }
        Set<Block> set = new HashSet<>();
        for (byte[] rlp: blocks) {
            set.add(new Block(rlp));
        }
        return set;
    }

    /**
     * remove all blocks and info of a chain
     * @param chainID
     * @throws Exception
     */
    @Override
    public void removeChain(byte[] chainID) throws Exception {
        db.removeWithKeyPrefix(chainID);
    }

    /**
     * get fork point block between main chain and fork chain
     * @param block
     * @return
     * @throws Exception
     */
    @Override
    public Block getForkPointBlock(Block block) throws Exception {
        byte[] chainID = block.getChainID();
        // if on main chain
        byte[] infoRLP = db.get(PrefixKey.blockInfoKey(block.getChainID(), block.getBlockNum()));
        if (null != infoRLP) {
            BlockInfos blockInfos = new BlockInfos(infoRLP);
            List<BlockInfo> list = blockInfos.getBlockInfoList();

            for (BlockInfo blockInfo : list) {
                if (Arrays.equals(blockInfo.getHash(), block.getBlockHash()) && blockInfo.isMainChain()) {
                    // return itself
                    return block;
                }
            }
        } else {
            logger.info("ChainID[{}]: Cannot find block info with current block.", chainID.toString());
        }

        byte[] rlp = db.get(PrefixKey.blockKey(chainID, block.getPreviousBlockHash()));
        // if previous is null, return
        if (null == rlp) {
            logger.error("ChainID[{}]: Cannot find previous block, hash[{}]",
                    chainID.toString(), Hex.toHexString(block.getPreviousBlockHash()));
            return null;
        }
        Block previousBlock = new Block(rlp);

        while (true) {
            infoRLP = db.get(PrefixKey.blockInfoKey(chainID, previousBlock.getBlockNum()));
            if (null == infoRLP) {
                logger.error("ChainID[{}]: Cannot find block info with this block number:{}",
                        chainID.toString(), previousBlock.getBlockNum());
                return null;
            }
            BlockInfos blockInfos = new BlockInfos(infoRLP);
            List<BlockInfo> list = blockInfos.getBlockInfoList();
            boolean found = false;
            for (BlockInfo blockInfo : list) {
                if (Arrays.equals(blockInfo.getHash(), previousBlock.getBlockHash())) {
                    // found block
                    found = true;
                    if (blockInfo.isMainChain()) {
                        // if this block is on main chain, got it
                        return previousBlock;
                    } else {
                        // if this block is not on main chain, look ahead
                        rlp = db.get(PrefixKey.blockKey(chainID, previousBlock.getPreviousBlockHash()));
                        // if previous is null, return
                        if (null == rlp) {
                            logger.error("ChainID[{}]: Cannot find previous block, hash[{}].",
                                    chainID.toString(), Hex.toHexString(previousBlock.getPreviousBlockHash()));
                            return null;
                        }
                        logger.info("ChainID[{}]: Seek previous block hash[{}].",
                                chainID.toString(), Hex.toHexString(previousBlock.getPreviousBlockHash()));
                        previousBlock = new Block(rlp);
                        break;
                    }
                }
            }
            // if not found this block info in this height
            if (!found) {
                logger.error("ChainID[{}]: Cannot find block info, hash[{}]",
                        chainID.toString(), Hex.toHexString(previousBlock.getBlockHash()));
                return null;
            }
        }
    }

    /**
     * get fork point block between chain 1 and chain 2
     *
     * @param chain1Block block on chain 1
     * @param chain2Block block on chain 2
     * @return fork point block or null if not found
     * @throws Exception
     */
    @Override
    public Block getForkPointBlock(Block chain1Block, Block chain2Block) throws Exception {
        byte[] chainID = chain1Block.getChainID();
        long maxLevel = Math.max(chain1Block.getBlockNum(), chain2Block.getBlockNum());

        // 1. First ensure that you are one the save level
        long currentLevel = maxLevel;
        Block chain1Line = chain1Block;
        if (chain1Block.getBlockNum() > chain2Block.getBlockNum()) {

            while (currentLevel > chain2Block.getBlockNum()) {
                chain1Line = getBlockByHash(chainID, chain1Line.getPreviousBlockHash());
                if (chain1Line == null)
                    return null;
                --currentLevel;
            }
        }

        Block chain2Line = chain2Block;
        if (chain2Block.getBlockNum() > chain1Block.getBlockNum()) {

            while (currentLevel > chain1Block.getBlockNum()) {
                chain2Line = getBlockByHash(chainID, chain2Line.getPreviousBlockHash());
                if (chain2Line == null)
                    return null;
                --currentLevel;
            }
        }

        // 2. Loop back on each level until common block
        while (!Arrays.equals(chain2Line.getBlockHash(), chain1Line.getBlockHash())) {
            chain2Line = getBlockByHash(chainID, chain2Line.getPreviousBlockHash());
            chain1Line = getBlockByHash(chainID, chain1Line.getPreviousBlockHash());

            if (chain1Line == null || chain2Line == null)
                return null;
        }

        if (Arrays.equals(chain2Line.getBlockHash(), chain1Line.getBlockHash())) {
            return chain1Block;
        }

        return null;
    }

    /**
     * get fork info
     *
     * @param forkBlock  fork point block
     * @param bestBlock current chain best block
     * @param undoBlocks blocks to roll back from high to low
     * @param newBlocks  blocks to connect from high to low
     * @return
     */
    @Override
    public boolean getForkBlocksInfo(Block forkBlock, Block bestBlock, List<Block> undoBlocks, List<Block> newBlocks) throws Exception{
        byte[] chainID = bestBlock.getChainID();
        long maxLevel = Math.max(bestBlock.getBlockNum(), forkBlock.getBlockNum());

        // 1. First ensure that you are one the save level
        long currentLevel = maxLevel;
        Block forkLine = forkBlock;
        if (forkBlock.getBlockNum() > bestBlock.getBlockNum()) {

            while (currentLevel > bestBlock.getBlockNum()) {
                newBlocks.add(forkLine);

                forkLine = getBlockByHash(chainID, forkLine.getPreviousBlockHash());
                if (forkLine == null)
                    return false;
                --currentLevel;
            }
        }

        Block bestLine = bestBlock;
        if (bestBlock.getBlockNum() > forkBlock.getBlockNum()) {

            while (currentLevel > forkBlock.getBlockNum()) {
                undoBlocks.add(bestLine);

                bestLine = getBlockByHash(chainID, bestLine.getPreviousBlockHash());
                --currentLevel;
            }
        }

        // 2. Loop back on each level until common block
        while (!Arrays.equals(bestLine.getBlockHash(), forkLine.getBlockHash())) {
            newBlocks.add(forkLine);
            undoBlocks.add(bestLine);

            bestLine = getBlockByHash(chainID, bestLine.getPreviousBlockHash());
            forkLine = getBlockByHash(chainID, forkLine.getPreviousBlockHash());

            if (forkLine == null)
                return false;

            --currentLevel;
        }

        return true;
    }

    /**
     * re-branch blocks
     *
     * @param undoBlocks move to non-main chain
     * @param newBlocks  move to main chain
     */
    @Override
    public void reBranchBlocks(List<Block> undoBlocks, List<Block> newBlocks) throws Exception {
        if (undoBlocks != null) {
            for (Block block : undoBlocks) {
                saveBlockInfo(block, false);
            }
        }

        if (newBlocks != null) {
            for (Block block : newBlocks) {
                saveBlockInfo(block, true);
            }
        }
    }


}

