package ua.adeptius.myapplications;

import org.junit.Test;

import ua.adeptius.myapplications.orders.Task;

import static org.junit.Assert.assertArrayEquals;

public class TaskTest {

    @Test
    public void phonesRegexTest(){
        Task task = new Task();
        task.setPhone("+380445121412 +380674081574");
        task.setComment("1. Модель роутера. TP-Link 841<stroka>2. Актуальный телефон. 093-942-" +
                "02-22<stroka>3. Уведомление о оплате. +<coment>Абонент просит на завтра в любое " +
                "время.<stroka>Заявка назначена на horoshilov<stroka>");
        String[] actualPhones = task.getPhones();

        String[] expectedPhones = new String[3];
        expectedPhones[0] = "0939420222";
        expectedPhones[1] = "0445121412";
        expectedPhones[2] = "0674081574";

        assertArrayEquals(actualPhones, expectedPhones);
    }
}
