/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.component.mock;

import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import org.apache.camel.BinaryPredicate;
import org.apache.camel.Exchange;
import org.apache.camel.Expression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents time-based clauses for setting expectations on the mocks. Such as time constrains for the received
 * messages.
 */
public class TimeClause implements BinaryPredicate {

    private static final Logger LOG = LoggerFactory.getLogger(TimeClause.class);

    private final Expression left;
    private final Expression right;

    private Time timeFrom;
    private Time timeTo;
    private boolean beforeNext;
    private String was;

    public TimeClause(Expression left, Expression right) {
        this.left = left;
        this.right = right;
    }

    // TimeUnit DSL
    // -------------------------------------------------------------------------

    public class TimeClassUnit {

        private final TimeClause clause;
        private final int from;
        private final int to;

        public TimeClassUnit(TimeClause clause, int to) {
            this(clause, -1, to);
        }

        public TimeClassUnit(TimeClause clause, int from, int to) {
            this.clause = clause;
            this.from = from;
            this.to = to;
        }

        public TimeClause millis() {
            period(TimeUnit.MILLISECONDS);
            return clause;
        }

        public TimeClause seconds() {
            period(TimeUnit.SECONDS);
            return clause;
        }

        public TimeClause minutes() {
            period(TimeUnit.MINUTES);
            return clause;
        }

        private void period(TimeUnit unit) {
            if (from > 0) {
                timeFrom = new Time(from, unit);
            }
            timeTo = new Time(to, unit);
        }
    }

    // DSL
    // -------------------------------------------------------------------------

    public TimeClassUnit noLaterThan(int period) {
        return new TimeClassUnit(this, period);
    }

    public TimeClassUnit between(int from, int to) {
        return new TimeClassUnit(this, from, to);
    }

    public void beforeNext() {
        this.beforeNext = true;
    }

    public void afterPrevious() {
        this.beforeNext = false;
    }

    // Implementation
    // -------------------------------------------------------------------------

    @Override
    public Expression getLeft() {
        return left;
    }

    @Override
    public Expression getRight() {
        return right;
    }

    @Override
    public boolean matches(Exchange exchange) {
        return matchesReturningFailureMessage(exchange) == null;
    }

    @Override
    public String matchesReturningFailureMessage(Exchange exchange) {
        // we must not store any state, so we can be thread-safe,
        // and thus we offer this method which returns a failure message if
        // we did not match
        String answer = null;

        // must be thread safe and store result in local objects
        Object leftValue = left.evaluate(exchange, Object.class);
        Object rightValue = right.evaluate(exchange, Object.class);
        if (!matches(exchange, leftValue, rightValue)) {
            answer = leftValue + " " + getOperator() + " " + rightValue;
        }

        return answer;
    }

    protected boolean matches(Exchange exchange, Object leftValue, Object rightValue) {
        was = null;
        boolean answer = true;

        if (timeTo == null) {
            throw new IllegalArgumentException("The time period has not been set. Ensure to include the time unit as well.");
        }

        Date currentDate = exchange.getProperty(Exchange.RECEIVED_TIMESTAMP, Date.class);

        final Date otherDate = getOtherDate(leftValue, rightValue);

        // if we could not grab the value, we hit a boundary (i.e., either 0 message or last message)
        if (otherDate == null) {
            return true;
        }

        // compute if we were within the allowed time range
        Time current = new Time(currentDate.getTime(), TimeUnit.MILLISECONDS);
        Time other = new Time(otherDate.getTime(), TimeUnit.MILLISECONDS);
        // must absolute delta as when we hit the boundaries, the delta would negative
        long delta = Math.abs(other.toMillis() - current.toMillis());
        was = "delta: " + delta + " millis";

        if (timeFrom != null) {
            long from = timeFrom.toMillis();
            answer = delta >= from;
        }
        if (answer) {
            long to = timeTo.toMillis();
            answer = delta <= to;
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Evaluated time clause [{}] with current: {}, other: {} -> {}", this, currentDate, otherDate,
                    answer);
        }

        return answer;
    }

    private Date getOtherDate(Object leftValue, Object rightValue) {
        // the other date is either the previous or the next
        Date otherDate;
        if (beforeNext) {
            // grab the previous value (left)
            if (leftValue != null) {
                otherDate = (Date) leftValue;
            } else {
                // we hit a boundary so grab the other
                otherDate = (Date) rightValue;
            }
        } else {
            // grab the next value (right)
            if (rightValue != null) {
                otherDate = (Date) rightValue;
            } else {
                // we hit a boundary so grab the other
                otherDate = (Date) leftValue;
            }
        }
        return otherDate;
    }

    @Override
    public String getOperator() {
        return getOperationText();
    }

    protected String getOperationText() {
        return beforeNext ? "before next" : "after previous";
    }

    @Override
    public String toString() {
        if (timeFrom == null) {
            return "no later than " + timeTo + " " + getOperationText() + " (" + was + ")";
        } else {
            return "between " + timeFrom.getNumber() + "-" + timeTo.getNumber() + " "
                   + timeTo.getTimeUnit().toString().toLowerCase(Locale.ENGLISH)
                   + " " + getOperationText() + " (" + was + ")";
        }
    }
}
