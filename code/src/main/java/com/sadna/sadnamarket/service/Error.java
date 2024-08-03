package com.sadna.sadnamarket.service;

import java.time.LocalTime;
import java.util.Set;
import java.util.TreeSet;

public class Error {
    public static String makeAuthPasswordIncorrectError(){
        return "password is incorrect";
    }

    public static String makeAuthUsernameExistsError(){
        return "username already exists";
    }

    public static String makeAuthUserDoesntExistError(){
        return "user doesnt exist";
    }

    public static String makeAuthInvalidJWTError(){
        return "jwt isnt valid";
    }

    public static String makeOrderNullError(){
        return "The order is null.";
    }

    public static String makeOrderEmptyError(){
        return "The order is empty.";
    }

    public static String makeOrderStoreNullError(int storeId){
        return "The store with ID: " + storeId + " has null order.";
    }

    public static String makeOrderStoreNoProductsError(int storeId){
        return "The store with ID: " + storeId + " has no products.";
    }

    public static String makeOrderStoreNoOrdersError(int storeId){
        return "The store with ID: " + storeId + " has no orders.";
    }

    public static String makeTokenInvalidError(String username){
        return String.format("Token is not valid for user %s.", username);
    }

    public static String makeOrderNameNullError(){
        return "The name is null.";
    }

    public static String makeOrderNameEmptyError(){
        return "The name is empty.";
    }

    public static String makeOrderNoOrdersForUserError(String username){
        return "There are no orders for "+username+".";
    }

    public static String makeOrderDoesntExistError(int orderId){
        return "The order: "+ orderId +" does not exist.";
    }

    public static String makeOrderNoOrdersError(){
        return "There are no orders.";
    }

    public static String makeProductDoesntExistError(int productId){
        return String.format("Product Id %d does not exist.", productId);
    }

    public static String makeProductAlreadyRemovedError(int productId){
        return String.format("Product Id %d was already removed.", productId);
    }

    public static String makeProductStoreIdInvalidError(int storeId){
        return String.format("Store Id %d is invalid.", storeId);
    }

    public static String makeProductDoesntExistInStoreError(int storeId, int productId){
        return String.format("Product %d does not exist in store %d.", productId, storeId);
    }

    public static String makeNotEnoughInStcokError(int storeId, int productId, int wantedAmount, int actualAmount){
        return String.format("You can not buy %d of product %d - there are only %d in stock in store %d.", wantedAmount, productId, actualAmount, storeId);
    }

    public static String makeProductAlreadyDeletedFromStoreError(int storeId, int productId){
        return String.format("Product Id %d was already deleted from store Id %d.", productId,
                storeId);
    }

    public static String makeProductAspectCannotBeNullOrEmptyError(String aspect){
        return "Product " + aspect +" cannot be null or empty.";
    }

    public static String makeProductMinimumPriceMustBeBelowMaximumError(){
        return "The minimum price must be below the maximum.";
    }

    public static String makeProductValuePriceCannotBeNegativeError(String valueType){
        return valueType + " product price cannot be negative.";
    }

    public static String makeProductRankHasToBeBetweenError(){
        return "Product rank has to be between 0 and 5.";
    }

    public static String makeStoreNoStoreWithIdError(int storeId){
        return String.format("There is no store with id %d.", storeId);
    }

    public static String makeStoreNoStoreWithNameError(String storeName){
        return String.format("There is no store with name %s.", storeName);
    }

    public static String makeStoreWithIdAlreadyExistsError(int storeId){
        return String.format("A store with the id %d already exists.", storeId);
    }

    public static String makeStoreWithNameAlreadyExistsError(String storeName){
        return String.format("A store with the name %s already exists.", storeName);
    }

    public static String makeStoreWithIdNotActiveError(int storeId){
        return String.format("A store with id %d is not active.", storeId);
    }

    public static String makeStoreClosedError(int storeId){
        return String.format("Store with id %d is closed.", storeId);
    }

    public static String makeStoreAlreadyClosedError(int storeId){
        return String.format("A store with id %d is already closed.", storeId);
    }

    public static String makeStoreProductAlreadyExistsError(int productId){
        return String.format("A product with id %d already exists.", productId);
    }

    public static String makeStoreIllegalProductAmountError(int amount){
        return String.format("%d is an illegal amount of products.", amount);
    }

    public static String makeStoreCartCannotBePurchasedError(){
        return "This cart can not be purchased.";
    }

    public static String makeStoreUserAlreadyOwnerError(String username, int storeId){
        return String.format("User %s is already a owner of store %d.", username, storeId);
    }

    public static String makeStoreUserAlreadyManagerError(String username, int storeId){
        return String.format("User %s is already a manager of store %d.", username, storeId);
    }

    public static String makeStoreOrderAlreadyExistsError(int storeId, int orderId){
        return String.format("A order with id %d already exists in store %d.", orderId, storeId);
    }

    public static String makeStoreUserHasToBeLoggedInError(String username){
        return String.format("User %s has to be logged in to create a store.", username);
    }

    public static String makeStoreUserCannotAddProductError(String username, int storeId){
        return String.format("user %s can not add a product to store with id %d.", username, storeId);
    }

    public static String makeStoreUserCannotDeleteProductError(String username, int storeId){
        return String.format("user %s can not delete a product from store with id %d.", username, storeId);
    }

    public static String makeStoreUserCannotUpdateProductError(String username, int storeId){
        return String.format("user %s can not update a product in store with id %d.", username, storeId);
    }

    public static String makeStoreUserCannotAddManagerError(String username, String newManagerUsername, int storeId){
        return String.format("User %s can not add user %s as a manager to store %d.",
                username, newManagerUsername, storeId);
    }

    public static String makeStoreUserCannotAddManagerPermissionsError(String username, String newManagerUsername, int storeId){
        return String.format("User %s can not edit user %s's manager permissions in store %d.",
                username, newManagerUsername, storeId);
    }

    public static String makeStoreUserCannotAddOwnerError(String username, String newOwnerUsername, int storeId){
        return String.format("User %s can not add user %s as an owner to store %d.",
                username, newOwnerUsername, storeId);
    }

    public static String makeStoreUserCannotCloseStoreError(String username, int storeId){
        return String.format("User %s can not close the store with id %d (not a founder).", username, storeId);
    }

    public static String makeStoreUserCannotGetRolesInfoError(String username, int storeId){
        return String.format(
                "A user %s is not an owner of store %d and can not request roles information.", username, storeId);
    }

    public static String makeStoreUserCannotStoreHistoryError(String username, int storeId){
        return String.format(
                "A user %s is not an owner of store %d and can not request order history.", username, storeId);
    }

    public static String makeStoreOfProductIsNotActiveError(int productId){
        return String.format("The store of product with id %d is not active.", productId);
    }

    public static String makeStoreUserCannotSetBankAccountError(String username, int storeId){
        return String.format("User %s cannot set bank account of store %d.",
                username, storeId);
    }

    public static String makeStoreProductDoesntExistError(int storeId, int productId){
        return String.format("A product with id %d does not exist in store %d.", productId, storeId);
    }

    public static String makeStoreUserCannotAddBuyPolicyError(String username, int storeId){
        return String.format("User %s can not add buy policy to store with id %d.", username, storeId);
    }

    public static String makeStoreUserCannotRemoveBuyPolicyError(String username, int storeId){
        return String.format("User %s can not remove buy policy from store with id %d.", username, storeId);
    }

    public static String makeStoreUserCannotAddDiscountPolicyError(String username, int storeId){
        return String.format("User %s can not add discount policy to store with id %d.", username, storeId);
    }

    public static String makeStoreUserCannotRemoveDiscountPolicyError(String username, int storeId){
        return String.format("User %s can not remove discount policy to store with id %d.", username, storeId);
    }

    public static String makeStoreNotValidAspectError(String value, String aspect){
        return String.format("%s is not a valid %s.", value, aspect);
    }

    public static String makeStoreOpeningHoursNotValid(){
        return "Opening or closing hours are not valid";
    }

    public static String makeBasketProductAlreadyExistsError(){
        return "product already exists in cart";
    }

    public static String makeBasketProductDoesntExistError(){
        return "product doesn't exist in cart";
    }

    public static String makeMemberDisallowedAppointError(){
        return "You disallowed appoint the one who appointed you!";
    }

    public static String makeMemberUserHasNoRoleError(){
        return "User has no role in this store";
    }

    public static String makeMemberUserIsNotLoggedInError(){
        return "user isn't logged in";
    }

    public static String makeMemberUserAlreadyHasRoleError(){
        return "member already has role in store";
    }

    public static String makeMemberUserDoesntExistError(String username){
        return "User with userName " + username + " does not exist";
    }

    public static String makeMemberGuestDoesntExistError(int guestId){
        return "Guest with guestID " + guestId + " does not exist";
    }

    public static String makeManagerDoesntHavePermissionError(){
        return "user doesnt has this permission";
    }

    public static String makeManagerYouAreNotAuthorizedError(){
        return "You are not authorized to perform this action";
    }

    public static String makeManagerNotRelevantError(){
        return "its not relvant who apointed you";
    }

    public static String makeManagerCannotAppointError(){
        return "manager doesnt has apointees";
    }

    public static String makeOwnerHasAllPermissionsError(){
        return "store owner has all the permissions";
    }

    public static String makeOwnerCannotRemovePermissionError(){
        return "can't remove permissions from a store owner";
    }

    public static String makeFounderCannotLeaveJobError(){
        return "owner cant leave the job";
    }

    public static String makeUserSystemManagerError(){
        return "only registered user can be system manager";
    }

    public static String makeUserLoggedInError(){
        return "user already logged in";
    }

    public static String makeUserCanOnlyEditPermissionsToApointeesError(){
        return "you can add permissions only to your appointers";
    }

    public static String makeValidStringError(){
        return "please enter valid string";
    }

    public static String makePurchaseMissingCardError(){
        return "Missing card details";
    }

    public static String makePurchaseMissingAddressError(){
        return "Missing address details";
    }

    public static String makePurchaseInvalidCardError(){
        return "Credit card is invalid";
    }

    public static String makePurchaseOrderCannotBeSuppliedError(){
        return "Order cannot be supplied";
    }

    public static String makePurchasePaymentCannotBeCompletedForStoreError(int storeId){
        return "Payment could not be completed for store " + storeId;
    }

    public static String makeUserDoesntExistError(){
        return "User doesnt exist in system";
    }

    public static String makeSystemManagerCanOnlyViewOrdersError(){
        return "Only system manager can view all orders";
    }

    public static String makeCartAmountAboveZeroError(){
        return "amount should be above 0";
    }

    public static String makeCartAmountDoesntExistError(){
        return "Amount doesn't exist in store";
    }

    public static String makeBuyPolicyWithIdDoesNotExistError(int buyPolicyId) {
        return String.format("A buy policy with id %d does not exist.", buyPolicyId);
    }

    public static String makeBuyPolicyAlreadyExistsError(int buyPolicyId) {
        return String.format("A buy policy with id %d already exists in store.", buyPolicyId);
    }

    public static String makeUserCanNotCreateBuyPolicyError(String username) {
        return String.format("User %s can not create buy policies.", username);
    }

    public static String makeBuyPolicyParamsError(String policyType, String min, String max) {
        return String.format("Invalid %s buy policy parameters: min: %s, max: %s.", policyType, min, max);
    }

    public static String makeBuyPolicyParamsError(String policyType, String params) {
        return String.format("Invalid %s buy policy parameters: %s.", policyType, params);
    }

    public static String makeCanNotRemoveLawBuyPolicyError(int policyId) {
        return String.format("%d is a law policy that can not be removed.", policyId);
    }

    public static String makeEmptyCategoryError() {
        return "The entered category is empty.";
    }

    public static String makeAgeLimitBuyPolicyError(String subject, int min, int max) {
        if(min == -1)
            return String.format("The user can not buy %s because his age is above %d.", subject, max);
        if(max == -1)
            return String.format("The user can not buy %s because his age is below %d.", subject, min);
        return String.format("The user can not buy %s because his age is not between %d and %d.", subject, min, max);
    }

    public static String makeAmountBuyPolicyError(String subject, int min, int max) {
        return String.format("The user can not buy %s because the amount is not between %d and %d.", subject, min, max);
    }

    public static String makeHolidayBuyPolicyError(String subject) {
        return "It is a holiday.";
    }

    public static String makeHourLimitBuyPolicyError(String subject, LocalTime from, LocalTime to) {
        return String.format("The user can not buy %s because the hour is not between %s and %s.", subject, from.toString(), to.toString());
    }

    public static String makeKgLimitBuyPolicyError(String subject, String from, String to) {
        return String.format("The user can not buy %s because the weight is not between %s and %s.", subject, from, to);
    }

    public static String makeRoshChodeshBuyPolicyError(String subject) {
        return String.format("The user can not buy %s because it is Rosh Chodesh - WAKE UP. IT'S THE FIRST OF THE MONTH.", subject);
    }

    public static String makeSpecificDateBuyPolicyError(String subject) {
        return String.format("The user can not buy %s on this specific date.", subject);
    }

    public static String makeAndBuyPolicyError(String policy1, String policy2) {
        return policy1 + " OR " + policy2;
    }

    public static String makeOrBuyPolicyError(String policy1, String policy2) {
        return policy1 + " AND " + policy2;
    }

    public static String makeConditioningBuyPolicyError(String policy1, String policy2) {
        return policy1 + " AND NOT " + policy2;
    }

    public static String makePermissionError(int num) {
        return String.format("No permission is associated with number %d.", num);
    }

    public static String makeUserCanNotCheckPermissionOfUserError(String actorUsername, String username, int storeId) {
        return String.format("%s can not permissions of % in store %d.", actorUsername, username, storeId);
    }

    public static String makePolicyProductsNotInStore(int storeId, Set<Integer> productIds, int policyId) {
        Set<Integer> sortedSet = new TreeSet<>(productIds);
        return String.format("Policy %d can not be added to store %d because the store does not have all of the policy products: %s.", policyId, storeId, sortedSet.toString());
    }

    public static String makeUserCanNotCreateDiscountPolicyError(String username){
        return String.format("%s can not create discounts policies.", username);
    }

    public static String makeUserCanNotCreateConditionForDiscountPolicyError(String username){
        return String.format("%s can not create condition for discounts policies.", username);
    }

    public static String percentageForDiscountIsNotInRange(double percentage) {
        return String.format("percentage discount is supposed to be between 100 and 0, yours is %f.", percentage);
    }

    public static String makeDiscountPolicyWithIdDoesNotExistError(int discountId) {
        return String.format("A Discount policy with id %d does not exist.", discountId);
    }

    public static String makeConditionWithIdDoesNotExistError(int condId) {
        return String.format("A Condition with id %d does not exist.", condId);
    }

    public static String makeDBError() {
        return "DB Error";
    }

    public static String makeNoStoreWithNameError(String name) {
        return String.format("There is no store called %s.", name);
    }

    public static String makeStoreDidNotSetBankAccountError(int storeId) {
        return String.format("Store %d did not set a bank account.", storeId);
    }

    public static String makeNoConditionWithIdExistError(int condId) {
        return String.format("condition %d does not exist.", condId);
    }

    public static String makeNoDiscountWithIdExistError(int policyId) {
        return String.format("Discount %d does not exist.", policyId);
    }

    public static String makeDiscountPolicyAlreadyExistsError(int discountPolicyId) {
        return String.format("Discount with id %d already exist this store %.", discountPolicyId);
    }

    public static String makeNoDiscountWithIdExistInStoreError(int policyId) {
        return String.format("Discount %d does not exist in this Store.", policyId);
    }

    public static String makeCannotRemoveDefaultDiscountFromStoreError(int discountPolicyId) {
        return String.format("Discount %d cannot be removed from this Store.", discountPolicyId);
    }

    public static String makeDatabaseTimeoutError() {
        return "There was a database timeout.";
    }

    public static String CannotMakeNegativeAmountCondition(int minAmount) {
        return "cannot make condition with negative minimum amount.";
    }

    public static String CannotMakeNegativeMinBuyCondition(int minBuy) {
        return "cannot make condition with negative min Buy.";
    }
}
