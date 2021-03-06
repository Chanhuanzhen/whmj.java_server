package org.mj.bizserver.cmdhandler.club;

import io.netty.channel.ChannelHandlerContext;
import org.mj.bizserver.allmsg.ClubServerProtocol;
import org.mj.bizserver.allmsg.InternalServerMsg;
import org.mj.bizserver.foundation.BizResultWrapper;
import org.mj.bizserver.mod.club.membercenter.MemberCenterBizLogic;
import org.mj.bizserver.mod.club.membercenter.bizdata.Player;
import org.mj.bizserver.mod.club.membercenter.bizdata.Table;
import org.mj.comm.cmdhandler.ICmdHandler;
import org.mj.comm.util.OutParam;

import java.util.List;
import java.util.Map;

/**
 * 获取牌桌列表指令处理器
 */
public class GetTableListCmdHandler implements ICmdHandler<ClubServerProtocol.GetTableListCmd> {
    @Override
    public void handle(
        ChannelHandlerContext ctx,
        int remoteSessionId,
        int fromUserId,
        ClubServerProtocol.GetTableListCmd cmdObj) {

        if (null == ctx ||
            remoteSessionId <= 0 ||
            fromUserId <= 0 ||
            null == cmdObj) {
            return;
        }

        final OutParam<Integer> out_maxNumOfTable = new OutParam<>();

        MemberCenterBizLogic.getInstance().getTableList_async(
            fromUserId,
            cmdObj.getClubId(),
            cmdObj.getPageIndex(),
            out_maxNumOfTable,
            (resultX) -> buildResultMsgAndSend(
                ctx, remoteSessionId, fromUserId, cmdObj.getClubId(), cmdObj.getPageIndex(), out_maxNumOfTable, resultX
            )
        );
    }

    /**
     * 构建结果消息并发送
     *
     * @param ctx               客户端信道处理器上下文
     * @param remoteSessionId   远程会话 Id
     * @param fromUserId        来自用户 Id
     * @param clubId            亲友圈 Id
     * @param pageIndex         页面索引
     * @param out_maxNumOfTable ( 输出参数 ) 最大牌桌数量
     * @param resultX           业务结果
     */
    static private void buildResultMsgAndSend(
        ChannelHandlerContext ctx,
        int remoteSessionId,
        int fromUserId,
        int clubId,
        int pageIndex,
        OutParam<Integer> out_maxNumOfTable,
        BizResultWrapper<List<Table>> resultX) {
        if (null == ctx ||
            null == resultX) {
            return;
        }

        final InternalServerMsg newMsg = new InternalServerMsg();
        newMsg.setRemoteSessionId(remoteSessionId);
        newMsg.setFromUserId(fromUserId);

        if (0 != newMsg.admitError(resultX)) {
            ctx.writeAndFlush(newMsg);
            return;
        }

        // 获取牌桌列表
        final List<Table> tableList = resultX.getFinalResult();

        ClubServerProtocol.GetTableListResult.Builder
            b0 = ClubServerProtocol.GetTableListResult.newBuilder();

        b0.setClubId(clubId)
            .setPageIndex(pageIndex)
            .setMaxNumOfTablez(out_maxNumOfTable.getVal());

        // 填充所有牌桌
        fillAllTable(b0, tableList);

        ClubServerProtocol.GetTableListResult r = b0.build();

        newMsg.putProtoMsg(r);
        ctx.writeAndFlush(newMsg);
    }

    /**
     * 填充所有牌桌
     *
     * @param b0        消息构建器
     * @param tableList 牌桌列表
     */
    static private void fillAllTable(
        final ClubServerProtocol.GetTableListResult.Builder b0,
        final List<Table> tableList) {
        if (null == b0 ||
            null == tableList ||
            tableList.isEmpty()) {
            return;
        }

        for (Table currTable : tableList) {
            if (null == currTable) {
                continue;
            }

            ClubServerProtocol.Table.Builder b1 = ClubServerProtocol.Table.newBuilder();
            b1.setSeqNum(currTable.getSeqNum())
                .setRoomId(currTable.getRoomId())
                .setGameType0(currTable.getGameType0IntVal())
                .setGameType1(currTable.getGameType1IntVal())
                .setMaxRound(currTable.getMaxRound())
                .setCurrRound(currTable.getCurrRound())
                .setMaxPlayer(currTable.getMaxPlayer());

            // 获取规则字典
            final Map<Integer, Integer> ruleMap = currTable.getRuleMap();

            for (Map.Entry<Integer, Integer> keyAndVal : ruleMap.entrySet()) {
                b1.addRuleItem(
                    ClubServerProtocol.KeyAndVal.newBuilder()
                        .setKey(keyAndVal.getKey())
                        .setVal(keyAndVal.getValue())
                );
            }

            // 填充所有玩家
            fillAllPlayer(b1, currTable.getPlayerList());
            // 添加亲友圈牌桌
            b0.addTable(b1);
        }
    }

    /**
     * 填充所有玩家
     *
     * @param b1         消息构建器
     * @param playerList 玩家列表
     */
    static private void fillAllPlayer(
        final ClubServerProtocol.Table.Builder b1,
        final List<Player> playerList) {
        if (null == b1 ||
            null == playerList ||
            playerList.isEmpty()) {
            return;
        }

        for (Player currPlayer : playerList) {
            if (null == currPlayer) {
                continue;
            }

            ClubServerProtocol.Player.Builder b2 = ClubServerProtocol.Player.newBuilder();
            b2.setUserId(currPlayer.getUserId())
                .setAtSeatIndex(currPlayer.getSeatIndex())
                .setUserName(currPlayer.getUserName())
                .setSex(currPlayer.getSex())
                .setHeadImg(currPlayer.getHeadImg());

            b1.addPlayer(b2);
        }
    }
}
