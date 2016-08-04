/*
 * Copyright (c) 2007-present, Stephen Colebourne & Michael Nascimento Santos
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 *  * Neither the name of JSR-310 nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.threeten.extra;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Test date range.
 */
@Test
public class TestLocalDateRange {

    private static final LocalDate DATE_2012_07_01 = LocalDate.of(2012, 7, 1);
    private static final LocalDate DATE_2012_07_27 = LocalDate.of(2012, 7, 27);
    private static final LocalDate DATE_2012_07_28 = LocalDate.of(2012, 7, 28);
    private static final LocalDate DATE_2012_07_29 = LocalDate.of(2012, 7, 29);
    private static final LocalDate DATE_2012_07_30 = LocalDate.of(2012, 7, 30);
    private static final LocalDate DATE_2012_07_31 = LocalDate.of(2012, 7, 31);
    private static final LocalDate DATE_2012_08_01 = LocalDate.of(2012, 8, 1);
    private static final LocalDate DATE_2012_08_31 = LocalDate.of(2012, 8, 31);

    //-----------------------------------------------------------------------
    public void test_ALL() {
        LocalDateRange test = LocalDateRange.ALL;
        assertEquals(test.getStart(), LocalDate.MIN);
        assertEquals(test.getEndInclusive(), LocalDate.MAX);
        assertEquals(test.getEnd(), LocalDate.MAX);
        assertEquals(test.isEmpty(), false);
        assertEquals(test.isUnboundedStart(), true);
        assertEquals(test.isUnboundedEnd(), true);
        assertEquals(test.toString(), "-999999999-01-01/+999999999-12-31");
    }

    //-----------------------------------------------------------------------
    public void test_of() {
        LocalDateRange test = LocalDateRange.of(DATE_2012_07_28, DATE_2012_07_31);
        assertEquals(test.getStart(), DATE_2012_07_28);
        assertEquals(test.getEndInclusive(), DATE_2012_07_30);
        assertEquals(test.getEnd(), DATE_2012_07_31);
        assertEquals(test.isEmpty(), false);
        assertEquals(test.isUnboundedStart(), false);
        assertEquals(test.isUnboundedEnd(), false);
        assertEquals(test.toString(), "2012-07-28/2012-07-31");
    }

    public void test_of_MIN() {
        LocalDateRange test = LocalDateRange.of(LocalDate.MIN, DATE_2012_07_31);
        assertEquals(test.getStart(), LocalDate.MIN);
        assertEquals(test.getEndInclusive(), DATE_2012_07_30);
        assertEquals(test.getEnd(), DATE_2012_07_31);
        assertEquals(test.isEmpty(), false);
        assertEquals(test.isUnboundedStart(), true);
        assertEquals(test.isUnboundedEnd(), false);
        assertEquals(test.toString(), "-999999999-01-01/2012-07-31");
    }

    public void test_of_MAX() {
        LocalDateRange test = LocalDateRange.of(DATE_2012_07_28, LocalDate.MAX);
        assertEquals(test.getStart(), DATE_2012_07_28);
        assertEquals(test.getEndInclusive(), LocalDate.MAX);
        assertEquals(test.getEnd(), LocalDate.MAX);
        assertEquals(test.isEmpty(), false);
        assertEquals(test.isUnboundedStart(), false);
        assertEquals(test.isUnboundedEnd(), true);
        assertEquals(test.toString(), "2012-07-28/+999999999-12-31");
    }

    public void test_of_MIN_MAX() {
        LocalDateRange test = LocalDateRange.of(LocalDate.MIN, LocalDate.MAX);
        assertEquals(test.getStart(), LocalDate.MIN);
        assertEquals(test.getEndInclusive(), LocalDate.MAX);
        assertEquals(test.getEnd(), LocalDate.MAX);
        assertEquals(test.isEmpty(), false);
        assertEquals(test.isUnboundedStart(), true);
        assertEquals(test.isUnboundedEnd(), true);
        assertEquals(test.toString(), "-999999999-01-01/+999999999-12-31");
    }

    public void test_of_MIN_MIN() {
        LocalDateRange test = LocalDateRange.of(LocalDate.MIN, LocalDate.MIN);
        assertEquals(test.getStart(), LocalDate.MIN);
        assertEquals(test.getEndInclusive(), LocalDate.MIN);
        assertEquals(test.getEnd(), LocalDate.MIN);
        assertEquals(test.isEmpty(), true);
        assertEquals(test.isUnboundedStart(), true);
        assertEquals(test.isUnboundedEnd(), false);
        assertEquals(test.toString(), "-999999999-01-01/-999999999-01-01");
    }

    public void test_of_empty() {
        LocalDateRange test = LocalDateRange.of(DATE_2012_07_30, DATE_2012_07_30);
        assertEquals(test.getStart(), DATE_2012_07_30);
        assertEquals(test.getEndInclusive(), DATE_2012_07_29);
        assertEquals(test.getEnd(), DATE_2012_07_30);
        assertEquals(test.isEmpty(), true);
        assertEquals(test.isUnboundedStart(), false);
        assertEquals(test.isUnboundedEnd(), false);
        assertEquals(test.toString(), "2012-07-30/2012-07-30");
    }

    @Test(expectedExceptions = DateTimeException.class)
    public void test_of_badOrder() {
        LocalDateRange.of(DATE_2012_07_31, DATE_2012_07_30);
    }

    //-----------------------------------------------------------------------
    public void test_ofClosed() {
        LocalDateRange test = LocalDateRange.ofClosed(DATE_2012_07_28, DATE_2012_07_30);
        assertEquals(test.getStart(), DATE_2012_07_28);
        assertEquals(test.getEndInclusive(), DATE_2012_07_30);
        assertEquals(test.getEnd(), DATE_2012_07_31);
        assertEquals(test.isUnboundedStart(), false);
        assertEquals(test.isUnboundedEnd(), false);
        assertEquals(test.toString(), "2012-07-28/2012-07-31");
    }

    public void test_ofClosed_MIN() {
        LocalDateRange test = LocalDateRange.ofClosed(LocalDate.MIN, DATE_2012_07_30);
        assertEquals(test.getStart(), LocalDate.MIN);
        assertEquals(test.getEndInclusive(), DATE_2012_07_30);
        assertEquals(test.getEnd(), DATE_2012_07_31);
        assertEquals(test.isUnboundedStart(), true);
        assertEquals(test.isUnboundedEnd(), false);
        assertEquals(test.toString(), "-999999999-01-01/2012-07-31");
    }

    public void test_ofClosed_MAX() {
        LocalDateRange test = LocalDateRange.ofClosed(DATE_2012_07_28, LocalDate.MAX);
        assertEquals(test.getStart(), DATE_2012_07_28);
        assertEquals(test.getEndInclusive(), LocalDate.MAX);
        assertEquals(test.getEnd(), LocalDate.MAX);
        assertEquals(test.isUnboundedStart(), false);
        assertEquals(test.isUnboundedEnd(), true);
        assertEquals(test.toString(), "2012-07-28/+999999999-12-31");
    }

    public void test_ofClosed_MIN_MAX() {
        LocalDateRange test = LocalDateRange.ofClosed(LocalDate.MIN, LocalDate.MAX);
        assertEquals(test.getStart(), LocalDate.MIN);
        assertEquals(test.getEndInclusive(), LocalDate.MAX);
        assertEquals(test.getEnd(), LocalDate.MAX);
        assertEquals(test.isUnboundedStart(), true);
        assertEquals(test.isUnboundedEnd(), true);
        assertEquals(test.toString(), "-999999999-01-01/+999999999-12-31");
    }

    @Test(expectedExceptions = DateTimeException.class)
    public void test_ofClosed_badOrder() {
        LocalDateRange.ofClosed(DATE_2012_07_31, DATE_2012_07_30);
    }

    //-----------------------------------------------------------------------
    public void test_of_period() {
        LocalDateRange test = LocalDateRange.of(DATE_2012_07_28, Period.ofDays(3));
        assertEquals(test.getStart(), DATE_2012_07_28);
        assertEquals(test.getEndInclusive(), DATE_2012_07_30);
        assertEquals(test.getEnd(), DATE_2012_07_31);
        assertEquals(test.isUnboundedStart(), false);
        assertEquals(test.isUnboundedEnd(), false);
        assertEquals(test.toString(), "2012-07-28/2012-07-31");
    }

    @Test(expectedExceptions = DateTimeException.class)
    public void test_of_period_negative() {
        LocalDateRange.of(DATE_2012_07_31, Period.ofDays(-1));
    }

    //-----------------------------------------------------------------------
    public void test_parse_CharSequence() {
        LocalDateRange test = LocalDateRange.parse(DATE_2012_07_27 + "/" + DATE_2012_07_29);
        assertEquals(test.getStart(), DATE_2012_07_27);
        assertEquals(test.getEnd(), DATE_2012_07_29);
    }

    public void test_parse_CharSequence_PeriodLocalDate() {
        LocalDateRange test = LocalDateRange.parse("P2D/" + DATE_2012_07_29);
        assertEquals(test.getStart(), DATE_2012_07_27);
        assertEquals(test.getEnd(), DATE_2012_07_29);
    }

    public void test_parse_CharSequence_PeriodLocalDate_case() {
        LocalDateRange test = LocalDateRange.parse("p2d/" + DATE_2012_07_29);
        assertEquals(test.getStart(), DATE_2012_07_27);
        assertEquals(test.getEnd(), DATE_2012_07_29);
    }

    public void test_parse_CharSequence_LocalDatePeriod() {
        LocalDateRange test = LocalDateRange.parse(DATE_2012_07_27 + "/P2D");
        assertEquals(test.getStart(), DATE_2012_07_27);
        assertEquals(test.getEnd(), DATE_2012_07_29);
    }

    public void test_parse_CharSequence_LocalDatePeriod_case() {
        LocalDateRange test = LocalDateRange.parse(DATE_2012_07_27 + "/p2d");
        assertEquals(test.getStart(), DATE_2012_07_27);
        assertEquals(test.getEnd(), DATE_2012_07_29);
    }

    public void test_parse_CharSequence_empty() {
        LocalDateRange test = LocalDateRange.parse(DATE_2012_07_27 + "/" + DATE_2012_07_27);
        assertEquals(test.getStart(), DATE_2012_07_27);
        assertEquals(test.getEnd(), DATE_2012_07_27);
    }

    @Test(expectedExceptions = DateTimeException.class)
    public void test_parse_CharSequence_badOrder() {
        LocalDateRange.parse(DATE_2012_07_29 + "/" + DATE_2012_07_27);
    }

    @Test(expectedExceptions = DateTimeParseException.class)
    public void test_parse_CharSequence_badFormat() {
        LocalDateRange.parse(DATE_2012_07_29 + "-" + DATE_2012_07_27);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void test_parse_CharSequence_null() {
        LocalDateRange.parse(null);
    }

    //-----------------------------------------------------------------------
    public void test_isSerializable() {
        assertTrue(Serializable.class.isAssignableFrom(LocalDateRange.class));
    }

    public void test_serialization() throws Exception {
        LocalDateRange original = LocalDateRange.of(DATE_2012_07_28, DATE_2012_07_31);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(baos);
        out.writeObject(original);
        out.close();
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream in = new ObjectInputStream(bais);
        LocalDateRange ser = (LocalDateRange) in.readObject();
        assertEquals(ser, original);
    }

    //-----------------------------------------------------------------------
    public void test_withStart() {
        LocalDateRange base = LocalDateRange.of(DATE_2012_07_28, DATE_2012_07_31);
        LocalDateRange test = base.withStart(DATE_2012_07_27);
        assertEquals(test.getStart(), DATE_2012_07_27);
        assertEquals(test.getEndInclusive(), DATE_2012_07_30);
        assertEquals(test.getEnd(), DATE_2012_07_31);
    }

    public void test_withStart_adjuster() {
        LocalDateRange base = LocalDateRange.of(DATE_2012_07_28, DATE_2012_07_31);
        LocalDateRange test = base.withStart(date -> date.minus(1, ChronoUnit.WEEKS));
        assertEquals(test.getStart(), DATE_2012_07_28.minusWeeks(1));
        assertEquals(test.getEndInclusive(), DATE_2012_07_30);
        assertEquals(test.getEnd(), DATE_2012_07_31);
    }

    public void test_withStart_min() {
        LocalDateRange base = LocalDateRange.of(DATE_2012_07_28, DATE_2012_07_31);
        LocalDateRange test = base.withStart(LocalDate.MIN);
        assertEquals(test.getStart(), LocalDate.MIN);
        assertEquals(test.getEndInclusive(), DATE_2012_07_30);
        assertEquals(test.getEnd(), DATE_2012_07_31);
    }

    public void test_withStart_empty() {
        LocalDateRange base = LocalDateRange.of(DATE_2012_07_28, DATE_2012_07_31);
        LocalDateRange test = base.withStart(DATE_2012_07_31);
        assertEquals(test.getStart(), DATE_2012_07_31);
        assertEquals(test.getEndInclusive(), DATE_2012_07_30);
        assertEquals(test.getEnd(), DATE_2012_07_31);
    }

    @Test(expectedExceptions = DateTimeException.class)
    public void test_withStart_invalid() {
        LocalDateRange base = LocalDateRange.of(DATE_2012_07_28, DATE_2012_07_30);
        base.withStart(DATE_2012_07_31);
    }

    //-----------------------------------------------------------------------
    public void test_withEnd() {
        LocalDateRange base = LocalDateRange.of(DATE_2012_07_28, DATE_2012_07_31);
        LocalDateRange test = base.withEnd(DATE_2012_07_30);
        assertEquals(test.getStart(), DATE_2012_07_28);
        assertEquals(test.getEndInclusive(), DATE_2012_07_29);
        assertEquals(test.getEnd(), DATE_2012_07_30);
    }

    public void test_withEnd_adjuster() {
        LocalDateRange base = LocalDateRange.of(DATE_2012_07_28, DATE_2012_07_31);
        LocalDateRange test = base.withEnd(date -> date.plus(1, ChronoUnit.WEEKS));
        assertEquals(test.getStart(), DATE_2012_07_28);
        assertEquals(test.getEndInclusive(), DATE_2012_07_30.plusWeeks(1));
        assertEquals(test.getEnd(), DATE_2012_07_31.plusWeeks(1));
    }

    public void test_withEnd_max() {
        LocalDateRange base = LocalDateRange.of(DATE_2012_07_28, DATE_2012_07_31);
        LocalDateRange test = base.withEnd(LocalDate.MAX);
        assertEquals(test.getStart(), DATE_2012_07_28);
        assertEquals(test.getEndInclusive(), LocalDate.MAX);
        assertEquals(test.getEnd(), LocalDate.MAX);
    }

    public void test_withEnd_empty() {
        LocalDateRange base = LocalDateRange.of(DATE_2012_07_30, DATE_2012_07_31);
        LocalDateRange test = base.withEnd(DATE_2012_07_30);
        assertEquals(test.getStart(), DATE_2012_07_30);
        assertEquals(test.getEndInclusive(), DATE_2012_07_29);
        assertEquals(test.getEnd(), DATE_2012_07_30);
    }

    @Test(expectedExceptions = DateTimeException.class)
    public void test_withEnd_invalid() {
        LocalDateRange base = LocalDateRange.of(DATE_2012_07_28, DATE_2012_07_31);
        base.withEnd(DATE_2012_07_27);
    }

    //-----------------------------------------------------------------------
    public void test_contains() {
        LocalDateRange test = LocalDateRange.of(DATE_2012_07_28, DATE_2012_07_31);
        assertEquals(test.contains(LocalDate.MIN), false);
        assertEquals(test.contains(DATE_2012_07_27), false);
        assertEquals(test.contains(DATE_2012_07_28), true);
        assertEquals(test.contains(DATE_2012_07_29), true);
        assertEquals(test.contains(DATE_2012_07_30), true);
        assertEquals(test.contains(DATE_2012_07_31), false);
        assertEquals(test.contains(DATE_2012_08_01), false);
        assertEquals(test.contains(LocalDate.MAX), false);
    }

    public void test_contains_empty() {
        LocalDateRange test = LocalDateRange.of(DATE_2012_07_28, DATE_2012_07_28);
        assertEquals(test.contains(LocalDate.MIN), false);
        assertEquals(test.contains(DATE_2012_07_27), false);
        assertEquals(test.contains(DATE_2012_07_28), false);
        assertEquals(test.contains(DATE_2012_07_29), false);
        assertEquals(test.contains(LocalDate.MAX), false);
    }

    public void test_contains_max() {
        LocalDateRange test = LocalDateRange.of(DATE_2012_07_28, LocalDate.MAX);
        assertEquals(test.contains(LocalDate.MIN), false);
        assertEquals(test.contains(DATE_2012_07_27), false);
        assertEquals(test.contains(DATE_2012_07_28), true);
        assertEquals(test.contains(DATE_2012_07_29), true);
        assertEquals(test.contains(DATE_2012_07_30), true);
        assertEquals(test.contains(DATE_2012_07_31), true);
        assertEquals(test.contains(DATE_2012_08_01), true);
        assertEquals(test.contains(LocalDate.MAX), true);
    }

    //-----------------------------------------------------------------------
    @DataProvider(name = "encloses")
    Object[][] data_encloses() {
        return new Object[][] {
            // before start
            { DATE_2012_07_01, DATE_2012_07_27, false, false }, { DATE_2012_07_01, DATE_2012_07_28, false, true },
            // before end
            { DATE_2012_07_27, DATE_2012_07_30, false, false }, { DATE_2012_07_28, DATE_2012_07_30, true, false },
            { DATE_2012_07_29, DATE_2012_07_30, true, false },
            // same end
            { DATE_2012_07_27, DATE_2012_07_31, false, false }, { DATE_2012_07_28, DATE_2012_07_31, true, false },
            { DATE_2012_07_29, DATE_2012_07_31, true, false }, { DATE_2012_07_30, DATE_2012_07_31, true, false },
            // past end
            { DATE_2012_07_27, DATE_2012_08_01, false, false }, { DATE_2012_07_28, DATE_2012_08_01, false, false },
            { DATE_2012_07_29, DATE_2012_08_01, false, false }, { DATE_2012_07_30, DATE_2012_08_01, false, false },
            // start past end
            { DATE_2012_07_31, DATE_2012_08_01, false, true }, { DATE_2012_07_31, DATE_2012_08_31, false, true },
            // empty
            { DATE_2012_07_27, DATE_2012_07_27, false, false }, { DATE_2012_07_28, DATE_2012_07_28, true, true },
            { DATE_2012_07_29, DATE_2012_07_29, true, false }, { DATE_2012_07_30, DATE_2012_07_30, true, false },
            { DATE_2012_07_31, DATE_2012_07_31, false, true }, { DATE_2012_08_31, DATE_2012_08_31, false, false },
            // min
            { LocalDate.MIN, DATE_2012_07_27, false, false }, { LocalDate.MIN, DATE_2012_07_28, false, true },
            { LocalDate.MIN, DATE_2012_07_29, false, false }, { LocalDate.MIN, DATE_2012_07_30, false, false },
            { LocalDate.MIN, DATE_2012_07_31, false, false }, { LocalDate.MIN, DATE_2012_08_01, false, false },
            { LocalDate.MIN, LocalDate.MAX, false, false },
            // max
            { DATE_2012_07_27, LocalDate.MAX, false, false }, { DATE_2012_07_28, LocalDate.MAX, false, false },
            { DATE_2012_07_29, LocalDate.MAX, false, false }, { DATE_2012_07_30, LocalDate.MAX, false, false },
            { DATE_2012_07_31, LocalDate.MAX, false, true }, { DATE_2012_08_01, LocalDate.MAX, false, false },
        };
    }

    @Test(dataProvider = "encloses")
    public void test_encloses(LocalDate start, LocalDate end, boolean isEnclosedBy, boolean abuts) {
        LocalDateRange test = LocalDateRange.of(DATE_2012_07_28, DATE_2012_07_31);
        assertEquals(test.encloses(LocalDateRange.of(start, end)), isEnclosedBy);
    }

    @Test(dataProvider = "encloses")
    public void test_abuts(LocalDate start, LocalDate end, boolean isEnclosedBy, boolean abuts) {
        LocalDateRange test = LocalDateRange.of(DATE_2012_07_28, DATE_2012_07_31);
        assertEquals(test.abuts(LocalDateRange.of(start, end)), abuts);
    }

    public void test_encloses_max() {
        LocalDateRange test = LocalDateRange.of(DATE_2012_07_28, LocalDate.MAX);
        assertEquals(test.encloses(LocalDateRange.of(DATE_2012_07_28, DATE_2012_07_28)), true);
        assertEquals(test.encloses(LocalDateRange.of(DATE_2012_07_28, DATE_2012_07_29)), true);
        assertEquals(test.encloses(LocalDateRange.of(DATE_2012_07_28, LocalDate.MAX)), true);
        assertEquals(test.encloses(LocalDateRange.of(DATE_2012_07_01, DATE_2012_07_27)), false);
        assertEquals(test.encloses(LocalDateRange.of(DATE_2012_07_27, DATE_2012_07_29)), false);
        assertEquals(test.encloses(LocalDateRange.of(DATE_2012_07_27, LocalDate.MAX)), false);
    }

    public void test_encloses_empty() {
        LocalDateRange test = LocalDateRange.of(DATE_2012_07_28, DATE_2012_07_28);
        assertEquals(test.encloses(LocalDateRange.of(DATE_2012_07_27, DATE_2012_07_27)), false);
        assertEquals(test.encloses(LocalDateRange.of(DATE_2012_07_28, DATE_2012_07_28)), false);
        assertEquals(test.encloses(LocalDateRange.of(DATE_2012_07_29, DATE_2012_07_29)), false);
        assertEquals(test.encloses(LocalDateRange.of(DATE_2012_07_27, LocalDate.MAX)), false);
        assertEquals(test.encloses(LocalDateRange.of(DATE_2012_07_28, LocalDate.MAX)), false);
    }

    //-----------------------------------------------------------------------
    @DataProvider(name = "noOverlap")
    Object[][] data_noOverlap() {
        return new Object[][] {
            { DATE_2012_07_01, DATE_2012_07_27, DATE_2012_07_27, DATE_2012_07_29 },
            { DATE_2012_07_01, DATE_2012_07_27, DATE_2012_07_29, DATE_2012_07_30 },
            { DATE_2012_07_01, DATE_2012_07_27, DATE_2012_07_29, DATE_2012_07_29 },
            { DATE_2012_07_01, DATE_2012_07_27, DATE_2012_07_27, DATE_2012_07_27 },
            { DATE_2012_07_01, DATE_2012_07_27, DATE_2012_07_28, DATE_2012_07_28 },
        };
    }

    @Test(dataProvider = "noOverlap")
    public void test_noOverlap(LocalDate start1, LocalDate end1, LocalDate start2, LocalDate end2) {
        LocalDateRange test1 = LocalDateRange.of(start1, end1);
        LocalDateRange test2 = LocalDateRange.of(start2, end2);
        assertEquals(test1.overlaps(test2), false);
    }

    @Test(dataProvider = "noOverlap", expectedExceptions = DateTimeException.class)
    public void test_noOverlap_intersection(LocalDate start1, LocalDate end1, LocalDate start2, LocalDate end2) {
        LocalDateRange test1 = LocalDateRange.of(start1, end1);
        LocalDateRange test2 = LocalDateRange.of(start2, end2);
        test1.intersection(test2);
    }

    @Test(dataProvider = "noOverlap", expectedExceptions = DateTimeException.class)
    public void test_noOverlap_union(LocalDate start1, LocalDate end1, LocalDate start2, LocalDate end2) {
        LocalDateRange test1 = LocalDateRange.of(start1, end1);
        LocalDateRange test2 = LocalDateRange.of(start2, end2);
        test1.union(test2);
    }

    @Test(dataProvider = "noOverlap")
    public void test_noOverlap_reverse(LocalDate start1, LocalDate end1, LocalDate start2, LocalDate end2) {
        LocalDateRange test1 = LocalDateRange.of(start1, end1);
        LocalDateRange test2 = LocalDateRange.of(start2, end2);
        assertEquals(test2.overlaps(test1), false);
    }

    @Test(dataProvider = "noOverlap", expectedExceptions = DateTimeException.class)
    public void test_noOverlap_reverse_intersection(LocalDate start1, LocalDate end1, LocalDate start2, LocalDate end2) {
        LocalDateRange test1 = LocalDateRange.of(start1, end1);
        LocalDateRange test2 = LocalDateRange.of(start2, end2);
        test2.intersection(test1);
    }

    @Test(dataProvider = "noOverlap", expectedExceptions = DateTimeException.class)
    public void test_noOverlap_reverse_union(LocalDate start1, LocalDate end1, LocalDate start2, LocalDate end2) {
        LocalDateRange test1 = LocalDateRange.of(start1, end1);
        LocalDateRange test2 = LocalDateRange.of(start2, end2);
        test2.union(test1);
    }

    public void test_overlaps_same() {
        LocalDateRange test = LocalDateRange.of(DATE_2012_07_28, DATE_2012_07_31);
        assertEquals(test.overlaps(test), true);
    }

    public void test_overlaps_empty() {
        LocalDateRange test = LocalDateRange.of(DATE_2012_07_28, DATE_2012_07_30);
        assertEquals(test.overlaps(LocalDateRange.of(DATE_2012_07_27, DATE_2012_07_27)), false);
        assertEquals(test.overlaps(LocalDateRange.of(DATE_2012_07_28, DATE_2012_07_28)), true);
        assertEquals(test.overlaps(LocalDateRange.of(DATE_2012_07_29, DATE_2012_07_29)), true);
        assertEquals(test.overlaps(LocalDateRange.of(DATE_2012_07_30, DATE_2012_07_30)), false);
        assertEquals(test.overlaps(LocalDateRange.of(DATE_2012_07_31, DATE_2012_07_31)), false);
    }

    public void test_overlaps_baseEmpty() {
        LocalDateRange test = LocalDateRange.of(DATE_2012_07_28, DATE_2012_07_28);
        assertEquals(test.overlaps(LocalDateRange.of(DATE_2012_07_27, DATE_2012_07_27)), false);
        assertEquals(test.overlaps(LocalDateRange.of(DATE_2012_07_28, DATE_2012_07_28)), false);
        assertEquals(test.overlaps(LocalDateRange.of(DATE_2012_07_29, DATE_2012_07_29)), false);
        assertEquals(test.overlaps(LocalDateRange.of(DATE_2012_07_28, DATE_2012_07_30)), true);
    }

    //-----------------------------------------------------------------------
    @DataProvider(name = "intersection")
    Object[][] data_intersection() {
        return new Object[][] {
            // overlap
            { DATE_2012_07_01, DATE_2012_07_29, DATE_2012_07_28, DATE_2012_07_30, DATE_2012_07_28, DATE_2012_07_29 },
            // encloses
            { DATE_2012_07_01, DATE_2012_07_30, DATE_2012_07_28, DATE_2012_07_29, DATE_2012_07_28, DATE_2012_07_29 },
            // encloses empty
            { DATE_2012_07_01, DATE_2012_07_30, DATE_2012_07_28, DATE_2012_07_28, DATE_2012_07_28, DATE_2012_07_28 },
        };
    }

    @Test(dataProvider = "intersection")
    public void test_intersection(
            LocalDate start1, LocalDate end1, LocalDate start2, LocalDate end2, LocalDate expStart, LocalDate expEnd) {

        LocalDateRange test1 = LocalDateRange.of(start1, end1);
        LocalDateRange test2 = LocalDateRange.of(start2, end2);
        LocalDateRange expected = LocalDateRange.of(expStart, expEnd);
        assertTrue(test1.overlaps(test2));
        assertEquals(test1.intersection(test2), expected);
    }

    @Test(dataProvider = "intersection")
    public void test_intersection_reverse(
            LocalDate start1, LocalDate end1, LocalDate start2, LocalDate end2, LocalDate expStart, LocalDate expEnd) {

        LocalDateRange test1 = LocalDateRange.of(start1, end1);
        LocalDateRange test2 = LocalDateRange.of(start2, end2);
        LocalDateRange expected = LocalDateRange.of(expStart, expEnd);
        assertTrue(test2.overlaps(test1));
        assertEquals(test2.intersection(test1), expected);
    }

    @DataProvider(name = "intersectionBad")
    Object[][] data_intersectionBad() {
        return new Object[][] {
            // adjacent
            { DATE_2012_07_01, DATE_2012_07_28, DATE_2012_07_28, DATE_2012_07_30, DATE_2012_07_28, DATE_2012_07_28 },
            // adjacent empty
            { DATE_2012_07_01, DATE_2012_07_30, DATE_2012_07_30, DATE_2012_07_30, DATE_2012_07_30, DATE_2012_07_30 },
        };
    }

    @Test(dataProvider = "intersectionBad", expectedExceptions = DateTimeException.class)
    public void test_intersectionBad(
            LocalDate start1, LocalDate end1, LocalDate start2, LocalDate end2, LocalDate expStart, LocalDate expEnd) {

        LocalDateRange test1 = LocalDateRange.of(start1, end1);
        LocalDateRange test2 = LocalDateRange.of(start2, end2);
        assertFalse(test1.overlaps(test2));
        test1.intersection(test2);
    }

    public void test_intersection_same() {
        LocalDateRange test = LocalDateRange.of(DATE_2012_07_28, DATE_2012_07_31);
        assertEquals(test.intersection(test), test);
    }

    //-----------------------------------------------------------------------
    @DataProvider(name = "union")
    Object[][] data_union() {
        return new Object[][] {
            // overlap
            { DATE_2012_07_01, DATE_2012_07_29, DATE_2012_07_28, DATE_2012_07_30, DATE_2012_07_01, DATE_2012_07_30 },
            // encloses
            { DATE_2012_07_01, DATE_2012_07_30, DATE_2012_07_28, DATE_2012_07_29, DATE_2012_07_01, DATE_2012_07_30 },
            // encloses empty
            { DATE_2012_07_01, DATE_2012_07_30, DATE_2012_07_28, DATE_2012_07_28, DATE_2012_07_01, DATE_2012_07_30 },
        };
    }

    @Test(dataProvider = "union")
    public void test_union(
            LocalDate start1, LocalDate end1, LocalDate start2, LocalDate end2, LocalDate expStart, LocalDate expEnd) {

        LocalDateRange test1 = LocalDateRange.of(start1, end1);
        LocalDateRange test2 = LocalDateRange.of(start2, end2);
        LocalDateRange expected = LocalDateRange.of(expStart, expEnd);
        assertTrue(test1.overlaps(test2));
        assertEquals(test1.union(test2), expected);
    }

    @Test(dataProvider = "union")
    public void test_union_reverse(
            LocalDate start1, LocalDate end1, LocalDate start2, LocalDate end2, LocalDate expStart, LocalDate expEnd) {

        LocalDateRange test1 = LocalDateRange.of(start1, end1);
        LocalDateRange test2 = LocalDateRange.of(start2, end2);
        LocalDateRange expected = LocalDateRange.of(expStart, expEnd);
        assertTrue(test2.overlaps(test1));
        assertEquals(test2.union(test1), expected);
    }

    @DataProvider(name = "unionBad")
    Object[][] data_unionBad() {
        return new Object[][] {
            // adjacent
            { DATE_2012_07_01, DATE_2012_07_28, DATE_2012_07_28, DATE_2012_07_30, DATE_2012_07_01, DATE_2012_07_30 },
            // adjacent empty
            { DATE_2012_07_01, DATE_2012_07_30, DATE_2012_07_30, DATE_2012_07_30, DATE_2012_07_01, DATE_2012_07_30 },
        };
    }

    @Test(dataProvider = "unionBad", expectedExceptions = DateTimeException.class)
    public void test_unionBad(
            LocalDate start1, LocalDate end1, LocalDate start2, LocalDate end2, LocalDate expStart, LocalDate expEnd) {

        LocalDateRange test1 = LocalDateRange.of(start1, end1);
        LocalDateRange test2 = LocalDateRange.of(start2, end2);
        assertFalse(test1.overlaps(test2));
        test1.union(test2);
    }

    public void test_union_same() {
        LocalDateRange test = LocalDateRange.of(DATE_2012_07_28, DATE_2012_07_31);
        assertEquals(test.union(test), test);
    }

    //-----------------------------------------------------------------------
    public void test_stream() {
        LocalDateRange test = LocalDateRange.of(DATE_2012_07_28, DATE_2012_07_31);
        List<LocalDate> result = test.stream().collect(Collectors.toList());
        assertEquals(result.size(), 3);
        assertEquals(result.get(0), DATE_2012_07_28);
        assertEquals(result.get(1), DATE_2012_07_29);
        assertEquals(result.get(2), DATE_2012_07_30);
    }

    //-----------------------------------------------------------------------
    @DataProvider(name = "isBefore")
    Object[][] data_isBefore() {
        return new Object[][] {
            // before start
            { DATE_2012_07_01, DATE_2012_07_27, false },
            // before end
            { DATE_2012_07_27, DATE_2012_07_30, false },
            { DATE_2012_07_28, DATE_2012_07_30, false },
            { DATE_2012_07_29, DATE_2012_07_30, false },
            // same end
            { DATE_2012_07_27, DATE_2012_07_31, false },
            { DATE_2012_07_28, DATE_2012_07_31, false },
            { DATE_2012_07_29, DATE_2012_07_31, false },
            { DATE_2012_07_30, DATE_2012_07_31, false },
            // past end
            { DATE_2012_07_27, DATE_2012_08_01, false },
            { DATE_2012_07_28, DATE_2012_08_01, false },
            { DATE_2012_07_29, DATE_2012_08_01, false },
            { DATE_2012_07_30, DATE_2012_08_01, false },
            // start past end
            { DATE_2012_07_31, DATE_2012_08_01, true },
            { DATE_2012_07_31, DATE_2012_08_31, true },
            // empty
            { DATE_2012_07_30, DATE_2012_07_30, false },
            { DATE_2012_07_31, DATE_2012_07_31, true },
            // min
            { LocalDate.MIN, DATE_2012_07_27, false },
            { LocalDate.MIN, DATE_2012_07_28, false },
            { LocalDate.MIN, DATE_2012_07_29, false },
            { LocalDate.MIN, DATE_2012_07_30, false },
            { LocalDate.MIN, DATE_2012_07_31, false },
            { LocalDate.MIN, DATE_2012_08_01, false },
            { LocalDate.MIN, LocalDate.MAX, false },
            // max
            { DATE_2012_07_27, LocalDate.MAX, false },
            { DATE_2012_07_28, LocalDate.MAX, false },
            { DATE_2012_07_29, LocalDate.MAX, false },
            { DATE_2012_07_30, LocalDate.MAX, false },
            { DATE_2012_07_31, LocalDate.MAX, true },
            { DATE_2012_08_01, LocalDate.MAX, true },
        };
    }

    @Test(dataProvider = "isBefore")
    public void test_isBefore_range(LocalDate start, LocalDate end, boolean before) {
        LocalDateRange test = LocalDateRange.of(DATE_2012_07_28, DATE_2012_07_31);
        assertEquals(test.isBefore(LocalDateRange.of(start, end)), before);
    }

    @Test(dataProvider = "isBefore")
    public void test_isBefore_date(LocalDate start, LocalDate end, boolean before) {
        LocalDateRange test = LocalDateRange.of(DATE_2012_07_28, DATE_2012_07_31);
        assertEquals(test.isBefore(start), before);
    }

    public void test_isBefore_range_empty() {
        LocalDateRange test = LocalDateRange.of(DATE_2012_07_29, DATE_2012_07_29);
        assertEquals(test.isBefore(LocalDateRange.of(DATE_2012_07_27, DATE_2012_07_28)), false);
        assertEquals(test.isBefore(LocalDateRange.of(DATE_2012_07_27, DATE_2012_07_29)), false);
        assertEquals(test.isBefore(LocalDateRange.of(DATE_2012_07_29, DATE_2012_07_29)), false);
        assertEquals(test.isBefore(LocalDateRange.of(DATE_2012_07_29, DATE_2012_07_30)), true);
        assertEquals(test.isBefore(LocalDateRange.of(DATE_2012_07_30, DATE_2012_07_30)), true);
        assertEquals(test.isBefore(LocalDateRange.of(DATE_2012_07_30, DATE_2012_07_31)), true);
    }

    public void test_isBefore_date_empty() {
        LocalDateRange test = LocalDateRange.of(DATE_2012_07_29, DATE_2012_07_29);
        assertEquals(test.isBefore(DATE_2012_07_28), false);
        assertEquals(test.isBefore(DATE_2012_07_29), false);
        assertEquals(test.isBefore(DATE_2012_07_30), true);
    }

    //-----------------------------------------------------------------------
    @DataProvider(name = "isAfter")
    Object[][] data_isAfter() {
        return new Object[][] {
            // before start
            { DATE_2012_07_01, DATE_2012_07_27, true },
            // to start
            { DATE_2012_07_01, DATE_2012_07_28, true },
            // before end
            { DATE_2012_07_01, DATE_2012_07_29, false },
            { DATE_2012_07_27, DATE_2012_07_30, false },
            { DATE_2012_07_28, DATE_2012_07_30, false },
            { DATE_2012_07_29, DATE_2012_07_30, false },
            // same end
            { DATE_2012_07_27, DATE_2012_07_31, false },
            { DATE_2012_07_28, DATE_2012_07_31, false },
            { DATE_2012_07_29, DATE_2012_07_31, false },
            { DATE_2012_07_30, DATE_2012_07_31, false },
            // past end
            { DATE_2012_07_27, DATE_2012_08_01, false },
            { DATE_2012_07_28, DATE_2012_08_01, false },
            { DATE_2012_07_29, DATE_2012_08_01, false },
            { DATE_2012_07_30, DATE_2012_08_01, false },
            // start past end
            { DATE_2012_07_31, DATE_2012_08_01, false },
            { DATE_2012_07_31, DATE_2012_08_31, false },
            // empty
            { DATE_2012_07_28, DATE_2012_07_28, true },
            { DATE_2012_07_29, DATE_2012_07_29, false },
            // min
            { LocalDate.MIN, DATE_2012_07_27, true },
            { LocalDate.MIN, DATE_2012_07_28, true },
            { LocalDate.MIN, DATE_2012_07_29, false },
            { LocalDate.MIN, DATE_2012_07_30, false },
            { LocalDate.MIN, DATE_2012_07_31, false },
            { LocalDate.MIN, DATE_2012_08_01, false },
            { LocalDate.MIN, LocalDate.MAX, false },
            // max
            { DATE_2012_07_27, LocalDate.MAX, false },
            { DATE_2012_07_28, LocalDate.MAX, false },
            { DATE_2012_07_29, LocalDate.MAX, false },
            { DATE_2012_07_30, LocalDate.MAX, false },
            { DATE_2012_07_31, LocalDate.MAX, false },
            { DATE_2012_08_01, LocalDate.MAX, false },
        };
    }

    @Test(dataProvider = "isAfter")
    public void test_isAfter_range(LocalDate start, LocalDate end, boolean before) {
        LocalDateRange test = LocalDateRange.of(DATE_2012_07_28, DATE_2012_07_31);
        assertEquals(test.isAfter(LocalDateRange.of(start, end)), before);
    }

    @Test(dataProvider = "isAfter")
    public void test_isAfter_date(LocalDate start, LocalDate end, boolean before) {
        LocalDateRange test = LocalDateRange.of(DATE_2012_07_28, DATE_2012_07_31);
        assertEquals(test.isAfter(end.minusDays(1)), before);
    }

    public void test_isAfter_range_empty() {
        LocalDateRange test = LocalDateRange.of(DATE_2012_07_29, DATE_2012_07_29);
        assertEquals(test.isAfter(LocalDateRange.of(DATE_2012_07_27, DATE_2012_07_28)), true);
        assertEquals(test.isAfter(LocalDateRange.of(DATE_2012_07_27, DATE_2012_07_29)), true);
        assertEquals(test.isAfter(LocalDateRange.of(DATE_2012_07_28, DATE_2012_07_28)), true);
        assertEquals(test.isAfter(LocalDateRange.of(DATE_2012_07_29, DATE_2012_07_29)), false);
        assertEquals(test.isAfter(LocalDateRange.of(DATE_2012_07_29, DATE_2012_07_30)), false);
        assertEquals(test.isAfter(LocalDateRange.of(DATE_2012_07_30, DATE_2012_07_30)), false);
        assertEquals(test.isAfter(LocalDateRange.of(DATE_2012_07_30, DATE_2012_07_31)), false);
    }

    public void test_isAfter_date_empty() {
        LocalDateRange test = LocalDateRange.of(DATE_2012_07_29, DATE_2012_07_29);
        assertEquals(test.isAfter(DATE_2012_07_28), true);
        assertEquals(test.isAfter(DATE_2012_07_29), false);
        assertEquals(test.isAfter(DATE_2012_07_30), false);
    }

  //-----------------------------------------------------------------------
    public void test_lengthInDays() {
        assertEquals(LocalDateRange.of(DATE_2012_07_27, DATE_2012_07_29).lengthInDays(), 2);
        assertEquals(LocalDateRange.of(DATE_2012_07_28, DATE_2012_07_29).lengthInDays(), 1);
        assertEquals(LocalDateRange.of(DATE_2012_07_29, DATE_2012_07_29).lengthInDays(), 0);
    }

    public void test_toPeriod() {
        assertEquals(LocalDateRange.of(DATE_2012_07_27, DATE_2012_07_29).toPeriod(), Period.ofDays(2));
        assertEquals(LocalDateRange.of(DATE_2012_07_28, DATE_2012_07_29).toPeriod(), Period.ofDays(1));
        assertEquals(LocalDateRange.of(DATE_2012_07_29, DATE_2012_07_29).toPeriod(), Period.ofDays(0));
    }

    //-----------------------------------------------------------------------
    public void test_equals() {
        LocalDateRange a = LocalDateRange.of(DATE_2012_07_27, DATE_2012_07_29);
        LocalDateRange a2 = LocalDateRange.of(DATE_2012_07_27, DATE_2012_07_29);
        LocalDateRange b = LocalDateRange.of(DATE_2012_07_27, DATE_2012_07_30);
        LocalDateRange c = LocalDateRange.of(DATE_2012_07_28, DATE_2012_07_29);
        assertEquals(a.equals(a), true);
        assertEquals(a.equals(a2), true);
        assertEquals(a.equals(b), false);
        assertEquals(a.equals(c), false);
        assertEquals(a.equals(null), false);
        assertEquals(a.equals(""), false);
        assertEquals(a.hashCode() == a2.hashCode(), true);
    }

}
