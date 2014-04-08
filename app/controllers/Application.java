package controllers;

import cache.CacheUnit;
import cache.ItemCache;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import com.hntoplinks.controller.HnController;

import jobs.EmailList;
import models.*;
import org.apache.commons.mail.EmailException;
import play.libs.Codec;
import utils.EmailUtil;

public class Application extends HnController {

    public static void today(Integer page) {
        renderArgs.put("activeTab", CacheUnit.today);
        if (page == null) {
            page = 1;
        }
        List<Item> items = ItemCache.getInstance().get(CacheUnit.today, page);
        renderArgs.put("items", items);
        renderArgs.put("page", page);
        render("Application/index.html");
    }

    public static void week(Integer page) {
        renderArgs.put("activeTab", CacheUnit.week);
        if (page == null) {
            page = 1;
        }
        List<Item> items = ItemCache.getInstance().get(CacheUnit.week, page);
        renderArgs.put("items", items);
        renderArgs.put("page", page);
        render("Application/index.html", items, page);
    }

    public static void month(Integer page) {
        renderArgs.put("activeTab", CacheUnit.month);
        if (page == null) {
            page = 1;
        }
        List<Item> items = ItemCache.getInstance().get(CacheUnit.month, page);
        renderArgs.put("items", items);
        renderArgs.put("page", page);
        render("Application/index.html", items, page);
    }

    public static void year(Integer page) {
        renderArgs.put("activeTab", CacheUnit.year);
        if (page == null) {
            page = 1;
        }
        List<Item> items = ItemCache.getInstance().get(CacheUnit.year, page);
        renderArgs.put("items", items);
        renderArgs.put("page", page);
        render("Application/index.html", items, page);
    }

    public static void all(Integer page) {
        renderArgs.put("activeTab", CacheUnit.all);
        if (page == null) {
            page = 1;
        }
        List<Item> items = ItemCache.getInstance().get(CacheUnit.all, page);
        renderArgs.put("items", items);
        renderArgs.put("page", page);
        render("Application/index.html", items, page);
    }

    public static void viewSubscription() {
        Subscription subscription = new Subscription();
        renderArgs.put("subscription", subscription);
        render("Application/subscription.html");
    }

    public static void viewModifySubscription(String subscriptionId) {
        Subscription subscription = Subscription.findBySubscriptionId(subscriptionId);
        if (subscription == null) {
            renderArgs.put("message", String.format("Subscription for id %s was not found", subscriptionId));
            render("Application/message.html");
        } else {
            renderArgs.put("subscription", subscription);
            render("Application/modify_subscription.html");
        }
    }

    public static void doSubscribe(Subscription subscription) {
        subscription.fixEmailFormat();
        validation.email(subscription.getEmail());
        validation.isTrue(subscription.isDaily() || subscription.isWeekly() || subscription.isMonthly() || subscription.isAnnually());

        subscription.setSubscriptionDate(Calendar.getInstance().getTime());
        subscription.setSubsUUID(Codec.UUID().toLowerCase());

        subscription.setActivationDate(null);
        if (!subscription.subscribedBefore()) {

            try {
                EmailUtil.sendActivationEmail(subscription, subscription.getEmail());
                subscription.setActivated(false);
            } catch (EmailException e) {
                subscription.setActivated(true);
                e.printStackTrace();
            } finally {
                subscription.save();
                renderArgs.put("subscription", subscription);
                render("Application/subscription_complete.html");
            }
        } else {
            renderArgs.put("subscription", subscription);
            render("Application/subscription.html");
        }
    }

    public static void modifySubscription(Subscription subscription) {
        Subscription subscriptionFromDB = Subscription.findBySubscriptionId(subscription.getSubsUUID());
        if (subscriptionFromDB == null) {
            renderArgs.put("message", String.format("Subscription for id %s was not found", subscription.getSubsUUID()));
            render("Application/message.html");
        } else {
            subscriptionFromDB.update(subscription);
            renderArgs.put("message", "Your subscription was updated.");
            render("Application/message.html");
        }
    }

    public static void unsubscribe(String subscriptionid) {
        Subscription.deleteSubscription(subscriptionid);
        String message = "You have unsubscribed. Bye...";
        renderArgs.put("message", message);
        render("Application/message.html");
    }


    public static void activate(String subscriptionid) {
        Subscription subscription = Subscription.findBySubscriptionId(subscriptionid);
        if (subscription == null) {
            renderArgs.put("message", String.format("Error!</br> Subscription id %s does not exist in our system.", subscriptionid));
            render("Application/message.html");
        } else {
            renderArgs.put("message", "Congratulations! <br/> Your subscription has been activated. <br/> You will receive periodic e-mail from now on.");
            render("Application/message.html");
        }
    }

    private static boolean checked(String value) {
        return "on".equals(value);
    }

    public static void about() {
        render("Application/about.html");
    }

}