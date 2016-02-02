/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package datasecurity;

import java.io.Serializable;

/**
 *
 * @author WorkSpace
 */
public class TestResults implements Serializable {
    public long firstHalfTime;
    public long fullTime;
    public double errorsPerSymbols;

    public TestResults( long firstHalfTime ,  long fullTime , double errorsPerSymbols ){
        this.firstHalfTime = firstHalfTime;
        this.fullTime = fullTime;
        this.errorsPerSymbols = errorsPerSymbols;
    }

}
