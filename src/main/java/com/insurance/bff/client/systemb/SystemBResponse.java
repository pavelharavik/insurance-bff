package com.insurance.bff.client.systemb;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

/**
 * Raw XML response returned by System B.
 *
 * <pre>
 * &lt;insurance id="123" first_name="John" last_name="Doe"
 *            birth_date="1990-01-01" is_active="true"/&gt;
 * </pre>
 *
 * {@code birth_date} is captured to mirror the upstream contract but is not
 * used by the BFF — the mapper ignores it.
 */
@JacksonXmlRootElement(localName = "insurance")
public record SystemBResponse(
        @JacksonXmlProperty(isAttribute = true)
        String id,

        @JacksonXmlProperty(isAttribute = true, localName = "first_name")
        String firstName,

        @JacksonXmlProperty(isAttribute = true, localName = "last_name")
        String lastName,

        @JacksonXmlProperty(isAttribute = true, localName = "birth_date")
        String birthDate,

        @JacksonXmlProperty(isAttribute = true, localName = "is_active")
        boolean active
) {}
