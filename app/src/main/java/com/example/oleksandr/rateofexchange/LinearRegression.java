package com.example.oleksandr.rateofexchange;

import java.util.List;

/**
 * Created by Oleksandr on 07.04.2016.
 */
public class LinearRegression {
    private double mBeta0;
    private double mBeta1;
    private double mR2;

    public LinearRegression(List<Float> yVals){
        final int MAXN = yVals.size();
        int n = 0;
        double[] x = new double[MAXN];
        double[] y = new double[MAXN];

        // first pass: read in data, compute xbar and ybar
        double sumx = 0.0, sumy = 0.0, sumx2 = 0.0;
        for(double item:yVals)
        {
            x[n] = n;
            y[n] = item;
            sumx  += x[n];
            sumx2 += x[n] * x[n];
            sumy  += y[n];
            n++;
        }

        double xbar = sumx / n;
        double ybar = sumy / n;

        // second pass: compute summary statistics
        double xxbar = 0.0, yybar = 0.0, xybar = 0.0;
        for (int i = 0; i < n; i++) {
            xxbar += (x[i] - xbar) * (x[i] - xbar);
            yybar += (y[i] - ybar) * (y[i] - ybar);
            xybar += (x[i] - xbar) * (y[i] - ybar);
        }
        mBeta1 = xybar / xxbar;
        mBeta0 = ybar - mBeta1 * xbar; //"y   = " + beta1 + " * x + " + beta0

        int df = n - 2;
        double rss = 0.0;      // residual sum of squares
        double ssr = 0.0;      // regression sum of squares
        for (int i = 0; i < n; i++) {
            double fit = mBeta1*x[i] + mBeta0;
            rss += (fit - y[i]) * (fit - y[i]);
            ssr += (fit - ybar) * (fit - ybar);
        }
        mR2    = ssr / yybar;
        double svar  = rss / df;
        double svar1 = svar / xxbar;
        double svar0 = svar/n + xbar*xbar*svar1;
    }

    public double getBeta0(){
        return mBeta0;
    }

    public double getBeta1(){
        return mBeta1;
    }

    public double getR2(){
        return mR2;
    }
}
