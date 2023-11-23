package com.widget.stock.k_line.view;


import com.widget.stock.k_line.data.KLineDataValid;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dingrui on 2016/10/27.
 * 计算指标
 */

public final class IndexCalculation {

    private boolean isStop = false;

    public void onStop() {
        isStop = true;
    }

    /**
     * 计算成交量包含ma5、ma10、ma20
     *
     * @param data
     * @param parameter
     * @return
     */
    public ArrayList<KLineDataValid> calculationMainMa(ArrayList<KLineDataValid> data, int[] parameter) {
        double ma1s = 0, ma2s = 0, ma3s = 0, ma4s = 0, ma5s = 0, ma6s = 0;
        double ma1 = 0, ma2 = 0, ma3 = 0, ma4 = 0, ma5 = 0, ma6 = 0;

        int p0 = parameter[0],
                p1 = parameter[1],
                p2 = parameter[2],
                p3 = parameter[3],
                p4 = parameter[4],
                p5 = parameter[5];
        int s0 = parameter[6],
                s1 = parameter[7],
                s2 = parameter[8],
                s3 = parameter[9],
                s4 = parameter[10],
                s5 = parameter[11];
        for (int i = 0; i < data.size(); i++) {
            if (isStop) {
                return null;
            }
            double close = data.get(i).getClose();
            ma1s += close;
            ma2s += close;
            ma3s += close;
            ma4s += close;
            ma5s += close;
            ma6s += close;
            if (s0 > 0)
                if (i >= p0 - 1 && p0 > 0) {
                    ma1 = ma1s / p0;
                    ma1s -= data.get(i - (p0 - 1)).getClose();
                }
            if (s1 > 0)
                if (i >= p1 - 1 && p1 > 0) {
                    ma2 = ma2s / p1;
                    ma2s -= data.get(i - (p1 - 1)).getClose();
                }
            if (s2 > 0)
                if (i >= p2 - 1 && p2 > 0) {
                    ma3 = ma3s / p2;
                    ma3s -= data.get(i - (p2 - 1)).getClose();
                }
            if (s3 > 0)
                if (i >= p3 - 1 && p3 > 0) {
                    ma4 = ma4s / p3;
                    ma4s -= data.get(i - (p3 - 1)).getClose();
                }
            if (s4 > 0)
                if (i >= p4 - 1 && p4 > 0) {
                    ma5 = ma5s / p4;
                    ma5s -= data.get(i - (p4 - 1)).getClose();
                }
            if (s5 > 0)
                if (i >= p5 - 1 && p5 > 0) {
                    ma6 = ma6s / p5;
                    ma6s -= data.get(i - (p5 - 1)).getClose();
                }
            KLineDataValid.MainMA mainMA = new KLineDataValid.MainMA(ma1, ma2, ma3, ma4, ma5, ma6);
            data.get(i).setMainMA(mainMA);
        }
        return data;
    }

    /**
     * 计算主图EMA
     * EMA(X，N)，求X的N日指数平滑移动平均。
     *
     * @param data
     * @param parameter
     * @return
     */
    public ArrayList<KLineDataValid> calculationMainEMA(ArrayList<KLineDataValid> data, int[] parameter) {
        double ema1 = 0, ema2 = 0, ema3 = 0, ema4 = 0, ema5 = 0, ema6 = 0;

        int p0 = parameter[0],
                p1 = parameter[1],
                p2 = parameter[2],
                p3 = parameter[3],
                p4 = parameter[4],
                p5 = parameter[5];
        int s0 = parameter[6],
                s1 = parameter[7],
                s2 = parameter[8],
                s3 = parameter[9],
                s4 = parameter[10],
                s5 = parameter[11];
        double preE1 = 0, preE2 = 0, preE3 = 0, preE4 = 0, preE5 = 0, preE6 = 0;
        if (data.size() > 0) {
            preE1 = data.get(0).getClose();
            preE2 = preE1;
            preE3 = preE1;
            preE4 = preE1;
            preE5 = preE1;
            ema1 = ema2 = ema3 = ema4 = ema6 = ema5 = preE6 = preE1;
        }
        for (int i = 0; i < data.size(); i++) {
            if (isStop) {
                return null;
            }
            if (i > 0) {
                KLineDataValid item = data.get(i);
                if (s0 > 0) {
                    ema1 = (2 * item.getClose() + (p0 - 1) * preE1) / (p0 + 1);
                    preE1 = ema1;
                }
                if (s1 > 0) {
                    ema2 = (2 * item.getClose() + (p1 - 1) * preE2) / (p1 + 1);
                    preE2 = ema2;
                }
                if (s2 > 0) {
                    ema3 = (2 * item.getClose() + (p2 - 1) * preE3) / (p2 + 1);
                    preE3 = ema3;
                }
                if (s3 > 0) {
                    ema4 = (2 * item.getClose() + (p3 - 1) * preE4) / (p3 + 1);
                    preE4 = ema4;
                }
                if (s4 > 0) {
                    ema5 = (2 * item.getClose() + (p4 - 1) * preE5) / (p4 + 1);
                    preE5 = ema5;
                }
                if (s5 > 0) {
                    ema6 = (2 * item.getClose() + (p5 - 1) * preE6) / (p5 + 1);
                    preE6 = ema6;
                }
            }
            KLineDataValid.MainEMA ema = new KLineDataValid.MainEMA(ema1, ema2, ema3, ema4, ema5, ema6);
            data.get(i).setMainEMA(ema);
        }
        return data;
    }

    /**
     * 计算成交量的MA指标，ma1、ma2、ma3
     *
     * @param data
     * @return
     */
    public ArrayList<KLineDataValid> calculationVol(ArrayList<KLineDataValid> data, int[] parameter) {
        double ma1s = 0;
        double ma2s = 0;
        double ma3s = 0;
        int p0 = parameter[0],
                p1 = parameter[1],
                p2 = parameter[2];
        for (int i = 0; i < data.size(); i++) {
            if (isStop) {
                return null;
            }
            double num = data.get(i).getVol();
            double ma1 = 0;
            double ma2 = 0;
            double ma3 = 0;
            ma1s += num;
            ma2s += num;
            ma3s += num;
            if (i >= p0 - 1 && p0 > 0) {
                ma1 = ma1s / p0;
                ma1s -= data.get(i - (p0 - 1)).getVol();
            }
            if (i >= p1 - 1 && p1 > 0) {
                ma2 = ma2s / p1;
                ma2s -= data.get(i - (p1 - 1)).getVol();
            }
            if (i >= p2 - 1 && p2 > 0) {
                ma3 = ma3s / p2;
                ma3s -= data.get(i - (p2 - 1)).getVol();
            }
            KLineDataValid.VolMA volMa = new KLineDataValid.VolMA(ma1, ma2, ma3);
            data.get(i).setVolMA(volMa);
        }
        return data;
    }

    /**
     * 计算MACD指标
     *
     * @param data
     * @return
     */
    public ArrayList<KLineDataValid> calculationMACD(ArrayList<KLineDataValid> data, int[] paremeter) {
        // MACD：参数快线移动平均、慢线移动平均、移动平均，
        // 参数值12、26、9。
        // 公式：⒈首先分别计算出收盘价12日指数平滑移动平均线与26日指数平滑移动平均线，分别记为EMA(12）与EMA(26）。
        // ⒉求这两条指数平滑移动平均线的差，即：DIFF=EMA（SHORT）－EMA（LONG）。
        // ⒊再计算DIFF的M日的平均的指数平滑移动平均线，记为DEA。
        // ⒋最后用DIFF减DEA，得MACD。MACD通常绘制成围绕零轴线波动的柱形图。MACD柱状大于0红色，小于0绿色。
        double ema12 = 0;
        double ema26 = 0;
        double oldEma12 = 0;
        double oldEma26 = 0;
        double diff = 0;
        double dea = 0;
        double refDEA = 0;
        double macd = 0;
        double sum = 0;
        double sumDif = 0;
        int p0 = paremeter[0],
                p1 = paremeter[1],
                p2 = paremeter[2];
        for (int i = 0; i < data.size(); i++) {
            if (isStop) {
                return null;
            }
            sum += data.get(i).getClose();
            if (i == p0 - 1 && p0 > 0) {
                ema12 = sum / p0;
                oldEma12 = ema12;
            } else if (i > p0 - 1 && p0 > 0) {
                ema12 = (2 * data.get(i).getClose() + (p0 - 1) * oldEma12) / (p0 + 1);
                oldEma12 = ema12;
            }
            if (i == p1 - 1 && p1 > 0) {
                ema26 = sum / p1;
                oldEma26 = ema26;
            } else if (i > p1 - 1 && p1 > 0) {
                ema26 = (2 * data.get(i).getClose() + (p1 - 1) * oldEma26) / (p1 + 1);
                oldEma26 = ema26;
            }
            if (i >= p1 - 1 && p1 > 0) {
                diff = ema12 - ema26;
                sumDif += diff;
                if (i == p1 + p2 - 2 && p2 > 0) {
                    dea = sumDif / p2;
                    macd = (diff - dea) * 2;
                    refDEA = dea;
                } else if (i > p1 + p2 - 2 && p2 > 0) {
                    dea = (2 * diff + (p2 - 1) * refDEA) / (p2 + 1);
                    refDEA = dea;
                    macd = (diff - dea) * 2;
                }
            }

            KLineDataValid.MACD mMacd = new KLineDataValid.MACD(diff, dea, macd);
            data.get(i).setMACD(mMacd);
        }
        return data;
    }

    /**
     * 计算MACD买卖指标
     *
     * @param data
     * @return
     */
    public ArrayList<KLineDataValid> calculationMACD_BS(ArrayList<KLineDataValid> data, int[] paremeter) {
        // MACD：参数快线移动平均、慢线移动平均、移动平均，
        // 参数值12、26、9。
        // 公式：⒈首先分别计算出收盘价12日指数平滑移动平均线与26日指数平滑移动平均线，分别记为EMA(12）与EMA(26）。
        // ⒉求这两条指数平滑移动平均线的差，即：DIFF=EMA（SHORT）－EMA（LONG）。
        // ⒊再计算DIFF的M日的平均的指数平滑移动平均线，记为DEA。
        // ⒋最后用DIFF减DEA，得MACD。MACD通常绘制成围绕零轴线波动的柱形图。MACD柱状大于0红色，小于0绿色。
        // 金叉：DIFF上穿DEA；
        // 死叉：DEA上穿DIFF；
        double ema12 = 0;
        double ema26 = 0;
        double oldEma12 = 0;
        double oldEma26 = 0;
        double diff = 0;
        double dea = 0;
        double refDEA = 0, refDIFF = 0;
        double macd = 0;
        double sum = 0;
        double sumDif = 0;
        int p0 = paremeter[0],
                p1 = paremeter[1],
                p2 = paremeter[2];
        boolean isDrawBuy, isDrawSell;
        for (int i = 0; i < data.size(); i++) {
            if (isStop) {
                return null;
            }
            isDrawBuy = false;
            isDrawSell = false;
            sum += data.get(i).getClose();
            if (i == p0 - 1 && p0 > 0) {
                ema12 = sum / p0;
                oldEma12 = ema12;
            } else if (i > p0 - 1 && p0 > 0) {
                ema12 = (2 * data.get(i).getClose() + (p0 - 1) * oldEma12) / (p0 + 1);
                oldEma12 = ema12;
            }
            if (i == p1 - 1 && p1 > 0) {
                ema26 = sum / p1;
                oldEma26 = ema26;
            } else if (i > p1 - 1 && p1 > 0) {
                ema26 = (2 * data.get(i).getClose() + (p1 - 1) * oldEma26) / (p1 + 1);
                oldEma26 = ema26;
            }
            if (i >= p1 - 1 && p1 > 0) {
                diff = ema12 - ema26;
                sumDif += diff;
                if (i == p1 + p2 - 2 && p2 > 0) {
                    dea = sumDif / p2;
                    macd = (diff - dea) * 2;
                    refDEA = dea;
                } else if (i > p1 + p2 - 2 && p2 > 0) {
                    dea = (2 * diff + (p2 - 1) * refDEA) / (p2 + 1);

                    if (diff > dea && refDIFF < refDEA
                            && diff > refDIFF) {
                        isDrawBuy = true;
                    } else if (diff < dea && refDIFF > refDEA
                            && diff < refDIFF) {
                        isDrawSell = true;
                    }

                    refDEA = dea;
                    macd = (diff - dea) * 2;
                }
                refDIFF = diff;
            }

            KLineDataValid.MACD_BS mMacd = new KLineDataValid.MACD_BS(diff, dea, macd, isDrawBuy, isDrawSell);
            data.get(i).setMACD_BS(mMacd);
        }
        return data;
    }

    /**
     * 计算BOLL指标
     *
     * @param data
     * @return
     */
    public ArrayList<KLineDataValid> calculationBOLL(ArrayList<KLineDataValid> data, int[] parameter) {
        // 指标参数20,2
        double closes = 0;// MA
        double MA = 0;// 中轨线
        double MD = 0;// 标准差
        double UP = 0;// 上轨线
        double DN = 0;// 下轨线
        int p0 = parameter[0];
        int p1 = parameter[1];
        for (int i = 0; i < data.size(); i++) {
            if (isStop) {
                return null;
            }
            closes += data.get(i).getClose();
            if (i >= p0 - 1 && p0 > 0 && p1 > 0) {
                MA = closes / p0;
                MD = getBollMD(data.subList(i - (p0 - 1), i + 1), MA, p0);
                UP = MA + p1 * MD;
                DN = MA - p1 * MD;
                closes -= data.get(i - (p0 - 1)).getClose();
            }
            KLineDataValid.MainBOLL boll = new KLineDataValid.MainBOLL(UP, MA, DN);
            data.get(i).setMainBOLL(boll);
        }
        return data;
    }

    /**
     * 计算KDJ
     *
     * @param data
     * @return
     */
    public ArrayList<KLineDataValid> calculationKDJ(ArrayList<KLineDataValid> data, int[] parameter) {
        // 默认参数9,3,3
//        RSV:=(CLOSE-LLV(LOW,N))/(HHV(HIGH,N)-LLV(LOW,N))*100;
//        K:SMA(RSV,M1,1);
//        D:SMA(K,M2,1);
//        J:3*K-2*D;

        double K = 0;
        double D = 0;
        double J = 0;
        int p0 = parameter[0],
                p1 = parameter[1],
                p2 = parameter[2];
        for (int i = 0; i < data.size(); i++) {
            if (isStop) {
                return null;
            }
            if (i >= p0 - 1 && p0 > 0) {
                double Cn = data.get(i).getClose();
                double Ln = getLow(data.subList(i - (p0 - 1), i + 1));
                if (Double.isNaN(Ln)) {
                    return null;
                }
                double Hn = getHigh(data.subList(i - (p0 - 1), i + 1));
                if (Double.isNaN(Hn)) {
                    return null;
                }
                double RSV = (Cn - Ln) / (Hn - Ln == 0 ? 1 : Hn - Ln) * 100;
                // 当日K值=2/3×前一日K值+1/3×当日RSV
                // 当日D值=2/3×前一日D值+1/3×当日K值
                // 若无前一日K 值与D值，则可分别用50来代替。
                // J值=3*当日K值-2*当日D值
                K = (p1 - 1.0) / (p1 * 1.0)
                        * (i == (p0 - 1) ? 50.0 : data.get(i - 1).getKDJ().getK())
                        + 1.0 / (p1 * 1.0) * RSV;
                D = (p2 - 1.0) / (p2 * 1.0)
                        * (i == 8 ? 50.0 : data.get(i - 1).getKDJ().getD())
                        + 1.0 / (p2 * 1.0) * K;
                J = 3.0 * K - 2.0 * D;
            }
            KLineDataValid.KDJ kdj = new KLineDataValid.KDJ(K, D, J);
            data.get(i).setKDJ(kdj);
        }
        return data;
    }

    /**
     * 计算KDJ买卖
     *
     * @param data
     * @return
     */
    public ArrayList<KLineDataValid> calculationKDJ_BS(ArrayList<KLineDataValid> data, int[] parameter) {
        // 默认参数9,3,3
//        RSV:=(CLOSE-LLV(LOW,N))/(HHV(HIGH,N)-LLV(LOW,N))*100;
//        K:SMA(RSV,M1,1);
//        D:SMA(K,M2,1);
//        J:3*K-2*D;

        double K = 0, refK = Double.NaN;// 上一周期K
        double D = 0, refD = 0;// 上一周期D
        double J = 0, refJ = 0;// 上一周期J
        int p0 = parameter[0],
                p1 = parameter[1],
                p2 = parameter[2];
        boolean isDrawBuy, isDrawSell;
        for (int i = 0; i < data.size(); i++) {
            if (isStop) {
                return null;
            }
            isDrawBuy = false;
            isDrawSell = false;
            if (i >= p0 - 1 && p0 > 0) {
                double Cn = data.get(i).getClose();
                double Ln = getLow(data.subList(i - (p0 - 1), i + 1));
                if (Double.isNaN(Ln)) {
                    return null;
                }
                double Hn = getHigh(data.subList(i - (p0 - 1), i + 1));
                if (Double.isNaN(Hn)) {
                    return null;
                }
                double RSV = (Cn - Ln) / (Hn - Ln == 0 ? 1 : Hn - Ln) * 100;
                // 当日K值=2/3×前一日K值+1/3×当日RSV
                // 当日D值=2/3×前一日D值+1/3×当日K值
                // 若无前一日K 值与D值，则可分别用50来代替。
                // J值=3*当日K值-2*当日D值
                K = (p1 - 1.0) / (p1 * 1.0)
                        * (i == (p0 - 1) ? 50.0 : data.get(i - 1).getKDJ_BS().getK())
                        + 1.0 / (p1 * 1.0) * RSV;
                D = (p2 - 1.0) / (p2 * 1.0)
                        * (i == 8 ? 50.0 : data.get(i - 1).getKDJ_BS().getD())
                        + 1.0 / (p2 * 1.0) * K;
                J = 3.0 * K - 2.0 * D;
                if (!Double.isNaN(refK)) {
                    if (refK < 40 && refD < 40 && refJ < 40) {
                        if (J > D && refJ < refD
                                && K > D && refK < refD
                                && J > refJ
                                && K > refK) {
                            isDrawBuy = true;
                        } else {
                            isDrawSell = false;
                        }
                    } else if (refK > 60 && refD > 60 && refJ > 60) {
                        if (D > J && refD < refJ
                                && D > K && refD < refK
                                && J < refJ
                                && K < refK) {
                            isDrawSell = true;
                        } else {
                            isDrawSell = false;
                        }
                    } else {
                        isDrawBuy = false;
                        isDrawSell = false;
                    }
                } else {
                    isDrawBuy = false;
                    isDrawSell = false;
                }
                refK = K;
                refD = D;
                refJ = J;
            }
            KLineDataValid.KDJ_BS kdj = new KLineDataValid.KDJ_BS(K, D, J, isDrawBuy, isDrawSell);
            data.get(i).setKDJ_BS(kdj);
        }
        return data;
    }

    /**
     * 计算RSI
     *
     * @param data
     * @return
     */
    public ArrayList<KLineDataValid> calculationRSI(ArrayList<KLineDataValid> data, int[] parameter) {
        // RSI：参数是6、12、24。
        // 公式：N日RSI =N日内收盘涨幅的平均值/(N日内收盘涨幅均值+N日内收盘跌幅均值) ×100%。
        // N日RSI =
        // N日内收盘涨幅的平均值/(N日内收盘涨幅均值+N日内收盘跌幅均值) ×100%
        double RSI1 = 0;// 参数6
        double RSI2 = 0;// 参数12
        double RSI3 = 0;// 参数24
        int p0 = parameter[0],
                p1 = parameter[1],
                p2 = parameter[2];

        double sumCloseA = 0;
        double sumCloseB = 0;

        double a1 = 0;
        double b1 = 0;
        double oldA1 = 0;
        double oldB1 = 0;

        double a2 = 0;
        double b2 = 0;
        double oldA2 = 0;
        double oldB2 = 0;

        double a3 = 0;
        double b3 = 0;
        double oldA3 = 0;
        double oldB3 = 0;

        for (int i = 0; i < data.size(); i++) {
            if (i > 0) {
                if (isStop) {
                    return null;
                }
                double tmp = data.get(i).getClose() - data.get(i - 1).getClose();
                if (tmp > 0) {
                    sumCloseA += tmp;
                } else {
                    sumCloseB += tmp;
                }
                double AA = tmp > 0 ? tmp : 0;
                double BB = Math.abs(tmp);

                if (i >= p0 && p0 > 0) {
                    if (i == p0) {
                        a1 = sumCloseA / p0;
                        b1 = (Math.abs(sumCloseB) + sumCloseA) / p0;
                        oldA1 = a1;
                        oldB1 = b1;
                    } else {
                        a1 = (AA + (p0 - 1) * oldA1) / p0;
                        b1 = (BB + (p0 - 1) * oldB1) / p0;
                        oldA1 = a1;
                        oldB1 = b1;
                    }
                    RSI1 = a1 / b1 * 100;
                }
                if (i >= p1 && p1 > 0) {
                    if (i == p1) {
                        a2 = sumCloseA / p1;
                        b2 = (Math.abs(sumCloseB) + sumCloseA) / p1;
                        oldA2 = a2;
                        oldB2 = b2;
                    } else {
                        a2 = (AA + (p1 - 1) * oldA2) / p1;
                        b2 = (BB + (p1 - 1) * oldB2) / p1;
                        oldA2 = a2;
                        oldB2 = b2;
                    }
                    RSI2 = a2 / b2 * 100;
                }
                if (i >= p2 && p2 > 0) {
                    if (i == p2) {
                        a3 = sumCloseA / p2;
                        b3 = (Math.abs(sumCloseB) + sumCloseA) / p2;
                        oldA3 = a3;
                        oldB3 = b3;
                    } else {
                        a3 = (AA + (p2 - 1) * oldA3) / p2;
                        b3 = (BB + (p2 - 1) * oldB3) / p2;
                        oldA3 = a3;
                        oldB3 = b3;
                    }
                    RSI3 = a3 / b3 * 100;
                }
            }
            KLineDataValid.RSI rsi = new KLineDataValid.RSI(RSI1, RSI2, RSI3);
            data.get(i).setRSI(rsi);
        }
        return data;
    }

    /**
     * 计算RSI买卖
     *
     * @param data
     * @return
     */
    public ArrayList<KLineDataValid> calculationRSI_BS(ArrayList<KLineDataValid> data, int[] parameter) {
        // RSI：参数是6、12、24。
        // 公式：N日RSI =N日内收盘涨幅的平均值/(N日内收盘涨幅均值+N日内收盘跌幅均值) ×100%。
        // N日RSI =
        // N日内收盘涨幅的平均值/(N日内收盘涨幅均值+N日内收盘跌幅均值) ×100%
//        LC:=REF(CLOSE,1);
//        RSI1:SMA(MAX(CLOSE-LC,0),N1,1)/SMA(ABS(CLOSE-LC),N1,1)*100;
//        RSI2:SMA(MAX(CLOSE-LC,0),N2,1)/SMA(ABS(CLOSE-LC),N2,1)*100;
//        RSI3:SMA(MAX(CLOSE-LC,0),N3,1)/SMA(ABS(CLOSE-LC),N3,1)*100;
//
//        REF_RSI1:=REF(RSI1,1);
//        DRAWTEXT(CROSS(RSI1,RSI2) && RSI1>REF_RSI1 && REF_RSI1<20,20,'买');
//        DRAWTEXT(CROSS(RSI2,RSI1) && RSI1<REF_RSI1 && REF_RSI1>80,80,'卖');
        double RSI1 = 0, refRSI1 = Double.NaN;// 参数6
        double RSI2 = 0, refRSI2 = Double.NaN;// 参数12
        double RSI3 = 0;// 参数24
        int p0 = parameter[0],
                p1 = parameter[1],
                p2 = parameter[2];

        double sumCloseA = 0;
        double sumCloseB = 0;

        double a1 = 0;
        double b1 = 0;
        double oldA1 = 0;
        double oldB1 = 0;

        double a2 = 0;
        double b2 = 0;
        double oldA2 = 0;
        double oldB2 = 0;

        double a3 = 0;
        double b3 = 0;
        double oldA3 = 0;
        double oldB3 = 0;
        boolean isRsi1 = false, isRsi2 = false;

        boolean isDrawBuy, isDrawSell;

        for (int i = 0; i < data.size(); i++) {
            isDrawBuy = false;
            isDrawSell = false;
            if (i > 0) {
                if (isStop) {
                    return null;
                }
                double tmp = data.get(i).getClose() - data.get(i - 1).getClose();
                if (tmp > 0) {
                    sumCloseA += tmp;
                } else {
                    sumCloseB += tmp;
                }
                double AA = tmp > 0 ? tmp : 0;
                double BB = Math.abs(tmp);

                if (i >= p0 && p0 > 0) {
                    if (i == p0) {
                        a1 = sumCloseA / p0;
                        b1 = (Math.abs(sumCloseB) + sumCloseA) / p0;
                        oldA1 = a1;
                        oldB1 = b1;
                    } else {
                        a1 = (AA + (p0 - 1) * oldA1) / p0;
                        b1 = (BB + (p0 - 1) * oldB1) / p0;
                        oldA1 = a1;
                        oldB1 = b1;
                    }
                    RSI1 = a1 / b1 * 100;
                    isRsi1 = true;
                }
                if (i >= p1 && p1 > 0) {
                    if (i == p1) {
                        a2 = sumCloseA / p1;
                        b2 = (Math.abs(sumCloseB) + sumCloseA) / p1;
                        oldA2 = a2;
                        oldB2 = b2;
                    } else {
                        a2 = (AA + (p1 - 1) * oldA2) / p1;
                        b2 = (BB + (p1 - 1) * oldB2) / p1;
                        oldA2 = a2;
                        oldB2 = b2;
                    }
                    RSI2 = a2 / b2 * 100;
                    isRsi2 = true;
                }
                if (i >= p2 && p2 > 0) {
                    if (i == p2) {
                        a3 = sumCloseA / p2;
                        b3 = (Math.abs(sumCloseB) + sumCloseA) / p2;
                        oldA3 = a3;
                        oldB3 = b3;
                    } else {
                        a3 = (AA + (p2 - 1) * oldA3) / p2;
                        b3 = (BB + (p2 - 1) * oldB3) / p2;
                        oldA3 = a3;
                        oldB3 = b3;
                    }
                    RSI3 = a3 / b3 * 100;
                }
                if (isRsi1 && isRsi2) {
                    if (!Double.isNaN(refRSI1)
                            && !Double.isNaN(refRSI2)) {
                        if (RSI1 > RSI2 && refRSI1 < refRSI2
                                && RSI1 > refRSI1
                                && refRSI1 < 40) {
                            isDrawBuy = true;
                        } else if (RSI1 < RSI2 && refRSI1 > refRSI2
                                && RSI1 < refRSI1
                                && refRSI1 > 60) {
                            isDrawSell = true;
                        }
                    }
                    refRSI1 = RSI1;
                    refRSI2 = RSI2;
                }
            }
            KLineDataValid.RSI_BS rsi = new KLineDataValid.RSI_BS(RSI1, RSI2, RSI3, isDrawBuy, isDrawSell);
            data.get(i).setRSI_BS(rsi);
        }
        return data;
    }

    /**
     * 计算BIAS指标
     *
     * @param data
     * @return
     */
    public ArrayList<KLineDataValid> calculationBIAS(ArrayList<KLineDataValid> data, int[] parameter) {
        // BIAS：参数是6、12、24。
        // 公式：BIAS=(收盘价-收盘价的N日简单平均)/收盘价的N日简单平均*100。
        // 乖离率=[(当日收盘价-N日平均价)/N日平均价]*100%
        // 参数：6，12、24
        double bias1 = 0;
        double bias2 = 0;
        double bias3 = 0;
        double closes1 = 0;
        double closes2 = 0;
        double closes3 = 0;
        int p0 = parameter[0],
                p1 = parameter[1],
                p2 = parameter[2];
        for (int i = 0; i < data.size(); i++) {
            if (isStop) {
                return null;
            }
            closes1 += data.get(i).getClose();
            closes2 += data.get(i).getClose();
            closes3 += data.get(i).getClose();
            if (i >= p0 - 1 && p0 > 0) {
                double mean6 = closes1 / p0;
                closes1 -= data.get(i - (p0 - 1)).getClose();
                bias1 = ((data.get(i).getClose() - mean6) / mean6) * 100;
            }
            if (i >= p1 - 1 && p1 > 0) {
                double mean12 = closes2 / p1;
                closes2 -= data.get(i - (p1 - 1)).getClose();
                bias2 = ((data.get(i).getClose() - mean12) / mean12) * 100;
            }
            if (i >= p2 - 1 && p2 > 0) {
                double mean24 = closes3 / p2;
                closes3 -= data.get(i - (p2 - 1)).getClose();
                bias3 = ((data.get(i).getClose() - mean24) / mean24) * 100;
            }
            KLineDataValid.BIAS bias = new KLineDataValid.BIAS(bias1, bias2, bias3);
            data.get(i).setBIAS(bias);
        }
        return data;
    }

    /**
     * 计算BRAR指标
     *
     * @param data
     * @return
     */
    public ArrayList<KLineDataValid> calculationBRAR(ArrayList<KLineDataValid> data, int[] parameter) {
        // 参数是26。
        // 公式N日BR=N日内（H－CY）之和除以N日内（CY－L）之和*100，
        // 其中，H为当日最高价，L为当日最低价，CY为前一交易日的收盘价，N为设定的时间参数。
        // N日AR=(N日内（H－O）之和除以N日内（O－L）之和)*100，
        // 其中，H为当日最高价，L为当日最低价，O为当日开盘价，N为设定的时间参数
        double BR = 0;
        double AR = 0;
        double HCY = 0;
        double CYL = 0;
        double HO = 0;
        double OL = 0;
        int p0 = parameter[0];
        int pm0 = p0 - 1;
        for (int i = 0; i < data.size(); i++) {
            if (isStop) {
                return null;
            }
            HO += (data.get(i).getHig() - data.get(i).getOpen());
            OL += (data.get(i).getOpen() - data.get(i).getLow());
            if (i > 0) {
                double CY = data.get(i - 1).getClose();
                HCY += (data.get(i).getHig() - CY) > 0 ? (data.get(i)
                        .getHig() - CY) : 0;
                CYL += (CY - data.get(i).getLow()) > 0 ? (CY - data.get(i)
                        .getLow()) : 0;
                if (i >= pm0 && p0 > 0) {
                    AR = HO / OL * 100;
                    HO -= data.get(i - pm0).getHig()
                            - data.get(i - pm0).getOpen();
                    OL -= data.get(i - pm0).getOpen()
                            - data.get(i - pm0).getLow();
                    if (i >= p0) {
                        BR = HCY / CYL * 100;
                        double CY1 = data.get(i - p0).getClose();
                        HCY -= (data.get(i - pm0).getHig() - CY1) > 0 ? (
                                data.get(i - pm0).getHig() - CY1) : 0;
                        CYL -= (CY1 - data.get(i - pm0).getLow()) > 0 ? (CY1 - data
                                .get(i - pm0).getLow()) : 0;
                    }
                }
            }
            KLineDataValid.BRAR brar = new KLineDataValid.BRAR(BR, AR);
            data.get(i).setBRAR(brar);
        }
        return data;
    }

    /**
     * 计算CCI指标
     *
     * @param data
     * @return
     */
    public ArrayList<KLineDataValid> calculationCCI(ArrayList<KLineDataValid> data, int[] parameter) {
        // CCI：参数是14。
        // 公式：中价与中价的N日内移动平均的差除以N日内中价的平均绝对偏差，
        // 其中，中价等于最高价、最低价和收盘价之和除以3。
        // ={【79-（79+62+45+90+25）/5）】
        // +【62-（79+62+45+90+25）/5）】
        // +【45-（79+62+45+90+25）/5）】
        // +【90-（79+62+45+90+25）/5）】
        // +【25-（79+62+45+90+25）/5）】}/5
        double TYPEs = 0;
        double cci = 0;
        int p0 = parameter[0];
        int pm0 = p0 - 1;
        for (int i = 0; i < data.size(); i++) {
            if (isStop) {
                return null;
            }
            double TYP = (data.get(i).getHig()
                    + data.get(i).getLow() + data.get(i).getClose()) / 3;
            TYPEs += TYP;
            if (i >= pm0 && p0 > 0) {
                double TYPEsMean = TYPEs / p0;
                TYPEs -= (data.get(i - pm0).getHig()
                        + data.get(i - pm0).getLow() + data.get(i - pm0)
                        .getClose()) / 3;

                double types = 0;
                for (int j = i - pm0; j < i + 1; j++) {
                    if (isStop) {
                        return null;
                    }
                    double typ = (data.get(j).getHig()
                            + data.get(j).getLow() + data.get(j)
                            .getClose()) / 3;
                    types += Math.abs(typ - TYPEsMean);
                }
                double MD = types / p0;
                if (MD == 0) {
                    cci = 0;
                } else {
                    cci = 200 * (TYP - TYPEsMean) / 3 / MD;
                }
            }
            KLineDataValid.CCI CCi = new KLineDataValid.CCI(cci);
            data.get(i).setCCI(CCi);
        }
        return data;
    }

    /**
     * 计算DMI
     *
     * @param data
     * @return
     */
    public ArrayList<KLineDataValid> calculationDMI(ArrayList<KLineDataValid> data, int[] parameter) {
        // 参数 14，6
        // MTR:=EXPMEMA(MAX(MAX(HIGH-LOW,ABS(HIGH-REF(CLOSE,1))),ABS(REF(CLOSE,1)-LOW)),N)
        // HD :=HIGH-REF(HIGH,1);
        // LD :=REF(LOW,1)-LOW;
        // DMP:=EXPMEMA(IF(HD>0&&HD>LD,HD,0),N);
        // DMM:=EXPMEMA(IF(LD>0&&LD>HD,LD,0),N);
        //
        // PDI: DMP*100/MTR;
        // MDI: DMM*100/MTR;
        // ADX: EXPMEMA(ABS(MDI-PDI)/(MDI+PDI)*100,MM);
        // ADXR:EXPMEMA(ADX,MM);
        // 公式含义：
        // MTR赋值:最高价-最低价和最高价-昨收的绝对值的较大值和昨收-最低价的绝对值的较大值的N日指数平滑移动平均 
        // HD赋值:最高价-昨日最高价 
        // LD赋值:昨日最低价-最低价 
        // DMP赋值:如果HD>0并且HD>LD,返回HD,否则返回0的N日指数平滑移动平均 
        // DMM赋值:如果LD>0并且LD>HD,返回LD,否则返回0的N日指数平滑移动平均 
        // 输出PDI: DMP*100/MTR 
        // 输出MDI: DMM*100/MTR 
        // 输出ADX: MDI-PDI的绝对值/(MDI+PDI)*100的MM日指数平滑移动平均 
        // 输出ADXR:ADX的MM日指数平滑移动平均
        double pdi = 0;
        double mdi = 0;
        double adx = 0;
        double adxr = 0;

        double HD = 0;
        double LD = 0;
        double refClose = 0;
        int p0 = parameter[0],
                p1 = parameter[1];
        List<Double> sumMax = new ArrayList<Double>();
        List<Double> sumMaxDmp = new ArrayList<Double>();
        List<Double> sumMaxDmm = new ArrayList<Double>();
        List<Double> sumAdx = new ArrayList<Double>();
        List<Double> sumAdxr = new ArrayList<Double>();
        for (int i = 0; i < data.size(); i++) {
            if (isStop) {
                return null;
            }
            if (i > 0) {
                refClose = data.get(i - 1).getClose();
                HD = data.get(i).getHig()
                        - data.get(i - 1).getHig();
                LD = data.get(i - 1).getLow() - data.get(i).getLow();

                double max1 = data.get(i).getHig()
                        - data.get(i).getLow() > Math.abs(data.get(i)
                        .getHig() -
                        refClose) ? data.get(i)
                        .getHig() -
                        data.get(i).getLow()
                        : Math
                        .abs(data.get(i).getHig() - refClose);
                double max2 = max1 > Math.abs(refClose)
                        - data.get(i).getLow() ? max1 : Math.abs(refClose)
                        -
                        data.get(i).getLow();
                sumMax.add(max2);

                double H;
                if (HD > 0 && HD > LD)
                    H = HD;
                else
                    H = 0;
                sumMaxDmp.add(H);

                double L;
                if (LD > 0 && LD > HD)
                    L = LD;
                else
                    L = 0;
                sumMaxDmm.add(L);

                if (i >= p0 && p0 > 0) {
                    double sumMax1 = 0;
                    double sumMaxDmp1 = 0;
                    double sumMaxDmm1 = 0;
                    for (int j = 0; j < sumMax.size(); j++) {
                        sumMax1 += sumMax.get(j);
                        sumMaxDmp1 += sumMaxDmp.get(j);
                        sumMaxDmm1 += sumMaxDmm.get(j);
                    }
                    sumMax.remove(0);
                    sumMaxDmp.remove(0);
                    sumMaxDmm.remove(0);
                    double mtr = sumMax1;
                    double dmp = sumMaxDmp1;
                    double dmm = sumMaxDmm1;

                    pdi = dmp * 100 / mtr;
                    mdi = dmm * 100 / mtr;
                    double adxN1 = Math.abs((mdi - pdi)) / (mdi + pdi) * 100;
                    sumAdx.add(adxN1);
                    if (i >= p0 + p1 - 1 && p1 > 0) {
                        double sum = 0;
                        for (int j = 0; j < sumAdx.size(); j++) {
                            sum += sumAdx.get(j);
                        }
                        adx = sum / p1;
                        sumAdx.remove(0);
                        sumAdxr.add(adx);
                        if (i >= p0 + p1 * 2 - 2) {
                            double sum1 = 0;
                            for (int j = 0; j < sumAdxr.size(); j++) {
                                sum1 += sumAdxr.get(j);
                            }
//                            sum1 += sumAdxr.get(0);
//                            sum1 += sumAdxr.get(sumAdxr.size() - 1);
//                            adxr = sum1 / 2;
                            adxr = sum1 / p1;
                            sumAdxr.remove(0);
                        }
                    }
                }
            }
            KLineDataValid.DMI dmi = new KLineDataValid.DMI(pdi, mdi, adx, adxr);
            data.get(i).setDMI(dmi);
        }
        return data;
    }

    /**
     * 计算CR
     *
     * @param data
     * @return
     */
    public ArrayList<KLineDataValid> calculationCR(ArrayList<KLineDataValid> data, int[] parameter) {
        // 参数26、5、10、20
        // MID:=REF(HIGH+LOW,1)/2;
        // CR:SUM(MAX(0,HIGH-MID),N)/SUM(MAX(0,MID-LOW),N)*100;
        // MA1:REF(MA(CR,M1),M1/2.5+1);
        // MA2:REF(MA(CR,M2),M2/2.5+1);
        // MA3:REF(MA(CR,M3),M3/2.5+1);
        // MID赋值:(昨日最高价+昨日最低价)/2
        // 输出带状能量线:0和最高价-MID的较大值的N日累和/0和MID-最低价的较大值的N日累和*100
        // 输出MA1:M1(5)/2.5+1日前的CR的M1(5)日简单移动平均
        // 输出MA2:M2(10)/2.5+1日前的CR的M2(10)日简单移动平均
        // 输出MA3:M3(20)/2.5+1日前的CR的M3(20)日简单移动平均
        // 输出MA4:M4/2.5+1日前的CR的M4日简单移动平均

        double cr = 0;
        double ma1 = 0;
        double ma2 = 0;
        double ma3 = 0;
        double P1 = 0;
        double P2 = 0;
        double ma1Index = 0;
        double ma2Index = 0;
        double ma3Index = 0;
        int p0 = parameter[0],
                pm0 = p0 - 1,
                p1 = parameter[1],
                pm1 = p1 - 1,
                p2 = parameter[2],
                pm2 = p2 - 1,
                p3 = parameter[3];
        int ref0 = (int) (p1 / 2.5f + 1);
        int r0 = pm0 + p1 + ref0;

        int ref1 = (int) (p2 / 2.5f + 1);
        int r1 = pm0 + p2 + ref1;

        int ref2 = (int) (p3 / 2.5f + 1);
        int r2 = pm0 + p3 + ref2;

        for (int i = 0; i < data.size(); i++) {
            if (isStop) {
                return null;
            }
            if (i > 0) {
                double YM = (data.get(i - 1).getHig()
                        + data.get(i - 1).getLow() + data.get(i - 1)
                        .getClose()) / 3;
                P1 += (0 >= data.get(i).getHig() - YM ? 0 : data.get(i)
                        .getHig() - YM);
                P2 += (0 >= YM - data.get(i).getLow() ? 0 : YM
                        - data.get(i).getLow());
                if (i >= p0 && p0 > 0) {
                    cr = P1 / P2 * 100;
                    ma1Index += cr;
                    ma2Index += cr;
                    ma3Index += cr;
                    double YM1 = (data.get(i - p0).getHig()
                            + data.get(i - p0).getLow() + data.get(i - p0)
                            .getClose()) / 3;
                    P1 -= 0 >= data.get(i - p0).getHig() - YM1 ? 0
                            : data.get(i - pm0).getHig() - YM1;
                    P2 -= 0 >= YM1 - data.get(i - pm0).getLow() ? 0
                            : YM1 - data.get(i - pm0).getLow();
                    if (i >= r0 && ref0 < p1) {
                        double ma1Index1 = ma1Index
                                - (getRefCR(data.subList(i - (ref0 - 1), i)) + cr);
                        ma1 = ma1Index1 / p1;
                        ma1Index -= data.get(i - (p1 + (ref0 - 1))).getCR().getCr();
                    }
                    if (i >= r1 && ref1 < p2) {
                        double ma2Index2 = ma2Index
                                - (getRefCR(data.subList(i - (ref1 - 1), i)) + cr);
                        ma2 = ma2Index2 / p2;
                        ma2Index -= data.get(i - (p2 + (ref1 - 1))).getCR().getCr();
                    }
                    if (i >= r2 && ref2 < p3) {
                        double ma3Index3 = ma3Index
                                - (getRefCR(data.subList(i - (ref2 - 1), i)) + cr);
                        ma3 = ma3Index3 / p3;
                        ma3Index -= data.get(i - (p3 + (ref2 - 1))).getCR().getCr();
                    }
                }
            }
            KLineDataValid.CR Cr = new KLineDataValid.CR(cr, ma1, ma2, ma3);
            data.get(i).setCR(Cr);
        }
        return data;
    }

    /**
     * 计算PSY
     *
     * @param data
     * @return
     */
    public ArrayList<KLineDataValid> calculationPSY(ArrayList<KLineDataValid> data, int[] parameter) {
        // PSY：参数是12。公式：PSY=N日内的上涨天数/N×100%。
        double psy = 0;
        double upDay = 0;
        int p0 = parameter[0];
        for (int i = 0; i < data.size(); i++) {
            if (isStop) {
                return null;
            }
            if (i > 0) {
                upDay += (data.get(i).getClose() - data.get(i - 1).getClose() > 0 ? 1
                        : 0);
                if (i >= p0 && p0 > 0) {
                    psy = upDay / p0 * 100;
                    upDay -= (data.get(i - (p0 - 1)).getClose()
                            - data.get(i - p0).getClose() > 0 ? 1 : 0);

                }
            }
            KLineDataValid.PSY Psy = new KLineDataValid.PSY(psy);
            data.get(i).setPSY(Psy);
        }
        return data;
    }

    /**
     * 计算DMA
     *
     * @param data
     * @return
     */
    public ArrayList<KLineDataValid> calculationDMA(ArrayList<KLineDataValid> data, int[] parameter) {
        // 参数是10、50、10。公式：DIF:MA(CLOSE,N1)-MA(CLOSE,N2);DIFMA:MA(DIF,M)
        double Dma = 0;
        double Ama = 0;
        double ma10Index = 0;
        double ma50Index = 0;
        double Dma10Index = 0;
        int p0 = parameter[0],
                p1 = parameter[1],
                p2 = parameter[2];
        for (int i = 0; i < data.size(); i++) {
            if (isStop) {
                return null;
            }
            ma10Index += data.get(i).getClose();
            ma50Index += data.get(i).getClose();

            if (i >= p1 - 1 && p1 > 0 && p0 > 0) {
                Dma = ma10Index / p0 - ma50Index / p1;
                Dma10Index += Dma;
                if (i >= p1 + p2 - 2 && p2 > 0) {
                    Ama = Dma10Index / p2;
                    Dma10Index -= data.get(i - (p2 - 1)).getDMA().getDif();
                }
                ma50Index -= data.get(i - (p1 - 1)).getClose();
            }
            if (i >= p0 - 1 && p0 > 0) {
                ma10Index -= data.get(i - (p0 - 1)).getClose();
            }
            KLineDataValid.DMA DMa = new KLineDataValid.DMA(Dma, Ama);
            data.get(i).setDMA(DMa);
        }
        return data;
    }

    /**
     * 计算TRIX
     *
     * @param data
     * @return
     */
    public ArrayList<KLineDataValid> calculationTRIX(ArrayList<KLineDataValid> data, int[] parameter) {
        // TR=收盘价的N日指数移动平均的N日指数移动平均的N日指数移动平均；
        // TRIX=(TR-昨日TR)/昨日TR*100；
        // MATRIX=TRIX的M日简单移动平均；
        // 参数N设为12，参数M设为20；
        // 参数12、20
        // 公式：MTR:=EMA(EMA(EMA(CLOSE,N),N),N)
        // TRIX:(MTR-REF(MTR,1))/REF(MTR,1)*100;
        // TRMA:MA(TRIX,M)
        double trix = 0;
        double maTrix = 0;
        double sumTrix = 0;

        double sumClose = 0;
        double emaClose = 0;
        double oldEmaClose = 0;
        double sumEmaClose = 0;
        double ema2 = 0;
        double oldEma2 = 0;
        double sumEma2 = 0;
        double ema3 = 0;
        double oldEma3 = 0;
        int p0 = parameter[0],
                p1 = parameter[1];
        int pm0 = p0 - 1;
        int pm1 = pm0 * 2;
        int pm2 = pm0 * 3;
        for (int i = 0; i < data.size(); i++) {
            if (isStop) {
                return null;
            }
            sumClose += data.get(i).getClose();
            if (i == pm0 && pm0 > 0) {
                emaClose = sumClose / p0;
                oldEmaClose = emaClose;
            } else if (i > pm0 && pm0 > 0) {
                emaClose = (2 * data.get(i).getClose() + (p0 - 1) * oldEmaClose) / (p0 + 1);
                oldEmaClose = emaClose;
            }
            sumEmaClose += emaClose;
            if (i == pm1) {
                ema2 = sumEmaClose / p0;
                oldEma2 = ema2;
            } else if (i > pm1) {
                ema2 = (2 * emaClose + (p0 - 1) * oldEma2) / (p0 + 1);
                oldEma2 = ema2;
            }
            sumEma2 += ema2;
            if (i == pm2) {
                ema3 = sumEma2 / p0;
                oldEma3 = ema3;
            } else if (i > pm2) {
                ema3 = (2 * ema2 + (p0 - 1) * oldEma3) / (p0 + 1);
                // 公式：MTR:=EMA(EMA(EMA(CLOSE,N),N),N)
                // TRIX:(MTR-REF(MTR,1))/REF(MTR,1)*100;
                // TRMA:MA(TRIX,M)
                trix = (ema3 - oldEma3) / oldEma3 * 100;
                sumTrix += trix;
                if (i >= pm2 + p1) {
                    maTrix = sumTrix / p1;
                    sumTrix -= data.get(i - (p1 - 1)).getTRIX().getTrix();
                }
                oldEma3 = ema3;
            }

            KLineDataValid.TRIX Trix = new KLineDataValid.TRIX(trix, maTrix);
            data.get(i).setTRIX(Trix);
        }
        return data;
    }

    /**
     * 计算收益率指标
     *
     * @param data
     * @return
     */
    public ArrayList<KLineDataValid> calculationReturnRate(ArrayList<KLineDataValid> data) {
        if (data != null && data.size() > 0) {
            double initNet = data.get(0).getClose();
            double rate, net;
            for (int i = 0; i < data.size(); i++) {
                if (isStop) {
                    return null;
                }
                net = data.get(i).getClose();
                rate = (net - initNet) / initNet * 100f;
                data.get(i).setReturnRate(new KLineDataValid.ReturnRate(rate));
            }
        }
        return data;
    }

    /**
     * 计算SAR指标
     *
     * @param data
     * @param parameter
     * @return
     */
    public ArrayList<KLineDataValid> calculationSAR(ArrayList<KLineDataValid> data, int[] parameter) {
        int p0 = parameter[0];
        double AF = 0.02f;
        boolean reverse = true;
        boolean up = true;
        List<Double> highList = new ArrayList<>();
        List<Double> lowList = new ArrayList<>();
        double lastSAR = -1;
        for (int i = 0; i < data.size(); i++) {
            if (isStop) {
                return null;
            }
            KLineDataValid item = data.get(i);
            if (highList.size() >= p0) {
                highList.remove(0);
                lowList.remove(0);
            }
            highList.add(item.getHig());
            lowList.add(item.getLow());
            if (!reverse && i != 0) {
                KLineDataValid lastItem = data.get(i - 1);
                if (up) {
                    AF = item.getHig() > lastItem.getHig()
                            ? AF + 0.02f > 0.2f ? 0.2f : AF + 0.02f
                            : AF;
                    lastSAR = lastSAR + AF * (lastItem.getHig() - lastSAR);
                    // lastSAR = Math.min(lastItem.low, item.low, lastSAR)
                    // 判断是否需要反转
                    if (item.getClose() <= lastSAR) {
                        reverse = true;
                        up = false;
                    }
                } else {
                    AF = item.getLow() < lastItem.getLow()
                            ? AF + 0.02f > 0.2f ? 0.2f : AF + 0.02f
                            : AF;
                    lastSAR = lastSAR + AF * (lastItem.getLow() - lastSAR);
                    // lastSAR = Math.max(lastItem.high, item.high, lastSAR)
                    // 判断是否需要反转
                    if (item.getClose() >= lastSAR) {
                        reverse = true;
                        up = true;
                    }
                }
            }
            if (reverse || i == 0) {
                double minData = lowList.get(0);
                for (int j = 0; j < lowList.size(); j++) {
                    minData = lowList.get(j) < minData ? lowList.get(j) : minData;
                }
                double maxData = highList.get(0);
                for (int j = 0; j < highList.size(); j++) {
                    maxData = highList.get(j) > maxData ? highList.get(j) : maxData;
                }
                lastSAR = up ? minData : maxData;
                AF = 0.02f;
                reverse = false;
            }
            item.setSar(new KLineDataValid.SAR(lastSAR, up));
        }

        return data;
    }

    /**
     * 计算布林指标中的标准差
     *
     * @param list
     * @param MA
     * @return
     */
    private double getBollMD(List<KLineDataValid> list, double MA, int para) {
        double sum = 0;
        for (int i = 0; i < list.size(); i++) {
            sum += Math.pow(list.get(i).getClose() - MA, 2);
        }
//        boolean b = sum > 0;
//        sum = Math.abs(sum);
//        double MD = Math.sqrt(sum / para);
//        return b ? MD : -1 * MD;
        return Math.sqrt(Math.abs(sum) / para);
    }

    /**
     * 获取list中的最大的最高价
     *
     * @param list
     * @return
     */
    private double getHigh(List<KLineDataValid> list) {
        double high = 0;
        if (list != null && list.size() > 0) {
            int size = list.size();
            high = list.get(0).getHig();
            for (int i = 1; i < size; i++) {
                if (isStop) {
                    return Double.NaN;
                }
                high = high < list.get(i).getHig() ? list.get(i)
                        .getHig() : high;
            }
        }
        return high;
    }

    /**
     * 获取N日内CR总和
     *
     * @param list
     * @return
     */
    private double getRefCR(List<KLineDataValid> list) {
        double allCR = 0;
        if (list != null) {
            for (int i = 0; i < list.size(); i++) {
                KLineDataValid.CR Cr = list.get(i).getCR();
                if (Cr == null) {
                    continue;
                }
                allCR += Cr.getCr();
            }
        }
        return allCR;
    }

    /**
     * 获取list中的最小的最低价
     *
     * @param list
     * @return
     */
    private double getLow(List<KLineDataValid> list) {
        double low = 0;
        if (list != null && list.size() > 0) {
            int size = list.size();
            low = list.get(0).getLow();
            for (int i = 1; i < size; i++) {
                if (isStop) {
                    return Double.NaN;
                }
                low = low > list.get(i).getLow() ? list.get(i)
                        .getLow() : low;
            }
        }
        return low;
    }

    /**
     * 获取N日内的涨和
     *
     * @param list
     * @return
     */
    private double getUpHe(List<KLineDataValid> list) {
        double up = 0;
        if (list != null && list.size() > 0) {
            int size = list.size();
            for (int i = 1; i < size; i++) {
                double z = list.get(i).getClose() - list.get(i - 1).getClose();
                up += z > 0 ? z : 0;
            }
        }
        return up;
    }

    /**
     * 获取N日内跌和
     *
     * @param list
     * @return
     */
    private double getDownHe(List<KLineDataValid> list) {
        double down = 0;
        if (list != null && list.size() > 0) {
            int size = list.size();
            for (int i = 1; i < size; i++) {
                double d = list.get(i).getClose() - list.get(i - 1).getClose();
                down += d < 0 ? Math.abs(d) : 0;
            }
        }
        return down;
    }

    /**
     * 比较较大值并返回
     *
     * @param d1
     * @param d2
     * @return
     */
    private double getMax(double d1, double d2) {
        if (d1 > d2)
            return d1;
        else
            return d2;
    }
}
