package id.kertas.smartrider.model;

import com.google.gson.annotations.SerializedName;

public class Message {
    @SerializedName("to")
    private String to;

    @SerializedName("message-price")
    private String messagePrice;

    @SerializedName("status")
    private String status;

    @SerializedName("message-id")
    private String messageId;

    @SerializedName("remaining-balance")
    private String remainingBalance;

    private String network;

    public String getTo ()
    {
        return to;
    }

    public void setTo (String to)
    {
        this.to = to;
    }


    public String getStatus ()
    {
        return status;
    }

    public void setStatus (String status)
    {
        this.status = status;
    }



    public String getNetwork ()
    {
        return network;
    }

    public void setNetwork (String network)
    {
        this.network = network;
    }

    public String getMessagePrice() {
        return messagePrice;
    }

    public void setMessagePrice(String messagePrice) {
        this.messagePrice = messagePrice;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getRemainingBalance() {
        return remainingBalance;
    }

    public void setRemainingBalance(String remainingBalance) {
        this.remainingBalance = remainingBalance;
    }

    @Override
    public String toString() {
        return "Message{" +
                "to='" + to + '\'' +
                ", messagePrice='" + messagePrice + '\'' +
                ", status='" + status + '\'' +
                ", messageId='" + messageId + '\'' +
                ", remainingBalance='" + remainingBalance + '\'' +
                ", network='" + network + '\'' +
                '}';
    }
}
