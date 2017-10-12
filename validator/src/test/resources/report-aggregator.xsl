<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema" exclude-result-prefixes="xs" version="2.0">
    <xsl:template match="node() | @*">
        <xsl:copy copy-namespaces="no">
            <xsl:apply-templates select="node() | @*"/>
        </xsl:copy>
    </xsl:template>
    <xsl:template match="/">
        <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0-beta/css/bootstrap.min.css"
              type="text/css"/>
        <table xsl:version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" class="table">
            <tr>
                <table>
                    <tr>
                        <td>Compliance Report for:
                            <xsl:variable name="resource"
                                          select="testsuites/testsuite[@name='JUnit Jupiter']/properties/property[@name='test.resource']/@value"/>
                            <a href="{$resource}" target="_blank">
                                <xsl:value-of select="$resource"/>
                            </a>
                        </td>
                    </tr>
                    <tr>
                        <td>Test Date:
                            <xsl:value-of select="testsuites/testsuite[@name='JUnit Jupiter']/@timestamp"/>
                        </td>
                    </tr>
                    <tr>
                        <td>Test Suite Duration:
                            <xsl:value-of select="testsuites/testsuite[@name='JUnit Jupiter']/@time"/>
                        </td>
                    </tr>
                    <tr>
                        <td>Test Suite Total Tests:
                            <xsl:value-of select="testsuites/testsuite[@name='JUnit Jupiter']/@tests"/>
                        </td>
                    </tr>
                </table>
            </tr>
            <tr>
                <table class="table">
                    <thead class="thead-inverse">
                        <tr>
                            <th>Test Class</th>
                            <th>Test Name</th>
                            <th>Failure Message</th>
                            <th>Failure Type</th>
                            <th>Error Message</th>
                            <th>Error Type</th>
                            <th>Test Duration</th>
                        </tr>
                    </thead>
                    <tbody>
                        <xsl:for-each-group select="testsuites/testsuite/testcase" group-by="@name">
                            <tr>
                                <td>
                                    <xsl:value-of select="./@classname"/>
                                </td>
                                <xsl:choose>
                                    <xsl:when test="./failure">
                                        <td bgcolor="red">
                                            <xsl:value-of select="substring-after(./system-out, 'display-name:')"/>
                                        </td>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <td bgcolor="green">
                                            <xsl:value-of select="substring-after(./system-out, 'display-name:')"/>
                                        </td>
                                    </xsl:otherwise>
                                </xsl:choose>
                                <td>
                                    <xsl:value-of select="./failure/@message"/>
                                </td>
                                <td>
                                    <xsl:value-of select="./failure/@type"/>
                                </td>
                                <td>
                                    <xsl:value-of
                                            select="./error/@message"
                                    />
                                </td>
                                <td>
                                    <xsl:value-of
                                            select="./error/@type"
                                    />
                                </td>
                                <td>
                                    <xsl:value-of
                                            select="sum(current-group()/@time) div count(current-group()/@time)"
                                    />
                                </td>
                            </tr>
                        </xsl:for-each-group>
                    </tbody>
                </table>
            </tr>
        </table>
    </xsl:template>
</xsl:stylesheet>
