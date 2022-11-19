// The doc comments in this class are processed to produce user-visible
// documentation as part of the package build process.  For this reason
// care should be taken to make the doc comment style comprehensible,
// consistent, concise, and not over-technical.

package uk.ac.starlink.ttools.func;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import uk.ac.starlink.ttools.build.HideDoc;
import uk.ac.starlink.ttools.jel.StateDependent;

/**
 * Functions concerned with random number generation.
 *
 * <p>There are two flavours of functions here:
 * index-based (<code>random*</code>) and sequential (<code>nextRandom*</code>).
 * Briefly, the index-based ones are safer to use, but provide poorer
 * random statistics, while the sequential ones provide decent randomness
 * but are not suitable for use in some/most contexts.
 * They are documented separately below.
 *
 * <p><strong>Index-based functions</strong></p>
 *
 * <p>The functions named <code>random*</code> all take an <code>index</code>
 * parameter which determines the value of the result;
 * the same index always leads to the same output,
 * but there is not supposed to be any obvious relationship between index
 * and output.
 * An explicit index is required to ensure that a given cell always has
 * the same value, since cell values are in general calculated on demand.
 * The quality of the randomness for these functions may not be that good.
 *
 * <p>In most cases, the table row index, available as the special token
 * <code>$0</code>, is a suitable value for the <code>index</code> parameter.
 *
 * <p>If several different random values are required in the same table row,
 * one way is to supply a different row-based index value for each one,
 * e.g. <code>random(2*$0)</code> and <code>random(2*$0+1)</code>.
 * However, this tends to introduce a correlation between the random
 * values in the same row, so a better (though in some cases slower) solution
 * is to use one of the array-generating functions, e.g.
 * <code>randomArray($0,2)[0]</code> and <code>randomArray($0,2)[1]</code>.
 *
 * <p>The output is deterministic, in the sense that the same invocation
 * will always generate the same "random" number, even across different
 * machines.  However, in view of the comments in the implementation note
 * below, the output <em>may</em> be subject to change in the future if
 * some improved algorithm can be found, so this guarantee does not
 * necessarily hold across software versions.
 *
 * <p><em>Implementation Note:</em>
 * The requirement for mapping a given input index deterministically
 * to a pseudo-random number constrains the way that the random number
 * generation is done; most well-studied RNGs generate sequences of
 * random numbers, but that approach cannot be used here, since these
 * sequences do not admit of random-access.
 * What we do instead is to scramble the input index somewhat and use that
 * as the seed for an instance of Java's <code>Random</code> class,
 * which is then used to produce one or more random numbers per input index.
 * Some thought and experimentation has gone into the current implementation
 * (I bought a copy of Knuth Vol. 2 specially!)
 * and an eyeball check of the results doesn't look all that bad,
 * but it's still probably not very good, and is not likely to pass
 * random number quality tests (though I haven't tried).
 * A more respectable approach <em>might</em> be to use a cryptographic-grade
 * hash function on the supplied index, but that's likely to be much slower.
 * If there is demand, something like that could be added as an alternative
 * option.  In the mean time, beware if you use these random numbers for
 * scientifically sensitive output.
 *
 * <p><strong>Sequential functions</strong></p>
 *
 * <p>The functions named <code>nextRandom*</code> have no arguments,
 * and supply the next value in a global sequence when they are evaluated.
 * These can be used if scanning through a table once (for instance when
 * writing a table using STILTS), but they are not suitable for contexts
 * that should supply a fixed value.
 * For instance if you use them to define the value of a table cell in TOPCAT,
 * that cell may have a different value every time you look at it,
 * which may have disconcerting results.
 * These use the java.util.Random class in a more standard way
 * than the index-based functions
 * and should provide random numbers of reasonable quality.
 *
 * @author   Mark Taylor
 * @since    19 Jan 2022
 */
public class Randoms {

    /**
     * Private constructor prevents instantiation.
     */
    private Randoms() {
    }

    /**
     * Generates a pseudo-random number
     * sampled from a uniform distribution between 0 and 1.
     *
     * <p>Note: The randomness may not be very high quality.
     *
     * @param   index  input value, typically row index "<code>$0</code>"
     * @return  random number between 0 and 1
     */
    public static double random( long index ) {
        return createRandomFromIndex( index ).nextDouble();
    }

    /**
     * Generates a pseudo-random number
     * sampled from a Gaussian distribution
     * with mean of 0.0 and standard deviation of 1.0.
     *
     * <p>Note: The randomness may not be very high quality.
     *
     * @param   index  input value, typically row index "<code>$0</code>"
     * @return  random number
     */
    public static double randomGaussian( long index ) {
        return createRandomFromIndex( index ).nextGaussian();
    }

    /**
     * Generates an array of pseudo-random numbers
     * sampled from a uniform distribution between 0 and 1.
     *
     * <p>Note: The randomness may not be very high quality.
     *
     * @param   index  input value, typically row index "<code>$0</code>"
     * @param   n    size of output array
     * @return  <code>n</code>-element array of random numbers between 0 and 1
     */
    public static double[] randomArray( long index, int n ) {
        Random rnd = createRandomFromIndex( index );
        double[] out = new double[ n ];
        for ( int i = 0; i < n; i++ ) {
            out[ i ] = rnd.nextDouble();
        }
        return out;
    }

    /**
     * Generates an array of pseudo-random numbers
     * sampled from a Gaussian distribution
     * with mean of 0.0 and standard deviation of 1.0.
     *
     * <p>Note: The randomness may not be very high quality.
     *
     * @param   index  input value, typically row index "<code>$0</code>"
     * @param   n    size of output array
     * @return  <code>n</code>-element array of random numbers
     */
    public static double[] randomGaussianArray( long index, int n ) {
        Random rnd = createRandomFromIndex( index );
        double[] out = new double[ n ];
        for ( int i = 0; i < n; i++ ) {
            out[ i ] = rnd.nextGaussian();
        }
        return out;
    }

    /**
     * Returns the next value in a random sequence,
     * sampled from a uniform distribution between 0 and 1.
     *
     * <p>This function will give a different result every time,
     * hence it is <em>not</em> suitable for use in an expression which
     * should have a fixed value, for instance to define a TOPCAT column.
     *
     * @return  random number between 0 and 1
     */
    @StateDependent
    public static double nextRandom() {
        return getGlobalRandom().nextDouble();
    }

    /**
     * Returns the next value in a random sequence,
     * sampled from a Gaussian distribution
     * with mean of 0.0 and standard deviation of 1.0.
     *
     * <p>This function will give a different result every time,
     * hence it is <em>not</em> suitable for use in an expression which
     * should have a fixed value, for instance to define a TOPCAT column.
     *
     * @return  random number
     */
    @StateDependent
    public static double nextRandomGaussian() {
        return getGlobalRandom().nextGaussian();
    }

    /**
     * Creates a <code>java.util.Random</code> object with a seed generated
     * deterministically from a supplied value that is typically a row index.
     * The supplied index is scrambled before being fed as a seed to the
     * Random constructor.
     *
     * <p>This function is not intended for general use,
     * in most cases one of the other functions is more appropriate.
     *
     * @param   index  input value, typically row index "<code>$0</code>"
     * @return  seeded Random object
     */
    @HideDoc
    public static Random createRandomFromIndex( long index ) {

        /* If we're going to use Java's RNG, we need a seed which must
         * be formed deterministically from the supplied index.
         * Using the index as the seed doesn't work very well,
         * given that it's expected to take the values 0, 1, 2, ...
         * the output doesn't look very random.
         * So scramble the index to form a seed and use that.
         * We do the scrambling using a Linear Congruential Generator.
         * So what we're doing is feeding the output of one RNG into
         * another RNG.  Knuth warns that that is not a good idea
         * (TAOCP Section 3), but for now I don't have a better one.
         * The results don't look all that bad, though I doubt they would
         * pass randomness tests. */
        long seed = lcgNext( index );

        /* The java.util.Random class only uses the lower 48 bits of the seed.
         * The best randomness of LCG output is supposed to be in the higher
         * bits, so I thought about shifting the upper bits downward before
         * passing it to Random.  However, this doesn't seem to help,
         * and some measures get worse, so comment this out for now. */
        if ( false ) {
            seed = Long.rotateRight( seed, 16 );
        }

        return new Random( seed );
    }

    /**
     * Returns a <code>java.util.Random</code> object with a globally
     * determined seed.
     *
     * <p>This function is not intended for general use,
     * in most cases one of the other functions is more appropriate.
     *
     * @return   seeded random object
     */
    @HideDoc
    public static Random getGlobalRandom() {
        return ThreadLocalRandom.current();
    }

    /**

     * Calculates the next value in the sequence of a Linear Congruential
     * Generator.
     *
     * @param   i   i'th value of a pseudo-random sequence 
     * @return  i+1'th value of the sequence
     */
    private static long lcgNext( long i ) {

        /* The parameters of this LCG are as used by Donald Knuth's MMIX;
         * that's according to
         * https://en.wikipedia.org/wiki/Linear_congruential_generator,
         * though I can't find those coefficients in Chapter 3 of
         * The Art Of Computer Programming (Vol 2 3rd Edition). */
        return 6364136223846793005L * i + 1442695040888963407L;
    }
}
