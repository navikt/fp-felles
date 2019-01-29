package no.nav.vedtak.felles.integrasjon.jms.pausing;

import javax.jms.JMSException;
import javax.jms.JMSRuntimeException;

import com.ibm.mq.MQException;
import com.ibm.mq.jmqi.JmqiException;
import com.ibm.msg.client.jms.JmsExceptionDetail;

public class MQExceptionUtil {

    private MQExceptionUtil() {

    }

    public static CharSequence extract(Exception je) {
        StringBuilder buf = new StringBuilder(300);

        // Henter ut kun MQ/JMS meldinger. Resten logges som vanlig Exception med cause.
        Throwable t = je;
        while (t != null) {

            if (t instanceof JMSException) { // NOSONAR
                startExceptionLine(je, buf, t);
                JMSException je1 = (JMSException) t;
                buf.append(",JMS Errorcode=").append(je1.getErrorCode()); //$NON-NLS-1$
                if (t instanceof JmsExceptionDetail) { // NOSONAR
                    JmsExceptionDetail jed = (JmsExceptionDetail) je1;
                    buf.append(",JMS Explanation=").append(jed.getExplanation()); //$NON-NLS-1$
                    buf.append(",JMS UserAction=").append(jed.getUserAction()); //$NON-NLS-1$
                }
                buf.append(';');
            } else if (t instanceof JMSRuntimeException) { // NOSONAR
                startExceptionLine(je, buf, t);
                JMSRuntimeException je1 = (JMSRuntimeException) t;
                buf.append(",JMS ErrorCode=").append(je1.getErrorCode()); //$NON-NLS-1$
                if (t instanceof JmsExceptionDetail) { // NOSONAR
                    JmsExceptionDetail jed = (JmsExceptionDetail) je1;
                    buf.append(",JMS Explanation=").append(jed.getExplanation()); //$NON-NLS-1$
                    buf.append(",JMS UserAction=").append(jed.getUserAction()); //$NON-NLS-1$
                }
                buf.append(';');
            } else if (t instanceof MQException) { // NOSONAR
                startExceptionLine(je, buf, t);
                MQException mqe = (MQException) t;
                buf.append(",WMQ CompletionCode=").append(mqe.getCompCode()); //$NON-NLS-1$
                buf.append(",WMQ ReasonCode=").append(mqe.getReason()); //$NON-NLS-1$
                buf.append(';');
            } else if (t instanceof JmqiException) { // NOSONAR
                startExceptionLine(je, buf, t);
                JmqiException jmqie = (JmqiException) t;
                buf.append(",WMQ LogMessage=").append(jmqie.getWmqLogMessage()); //$NON-NLS-1$
                buf.append(",WMQ Explanation=").append(jmqie.getWmqMsgExplanation()); //$NON-NLS-1$
                buf.append(",WMQ MsgSummary=").append(jmqie.getWmqMsgSummary()); //$NON-NLS-1$
                buf.append(",WMQ MsgUserResponse=").append(jmqie.getWmqMsgUserResponse()); //$NON-NLS-1$
                buf.append(",WMQ Msg Severity=").append(jmqie.getWmqMsgSeverity()); //$NON-NLS-1$
                buf.append(';');
            }

            // Get the next cause
            t = t.getCause();
        }

        return buf;
    }

    private static void startExceptionLine(Exception je, StringBuilder buf, Throwable t) {
        buf.append("ex=").append(t.getClass().getName()).append(',').append(je.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
    }

}
