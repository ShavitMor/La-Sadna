import { createContext } from "react";
import StoreModel from "./models/StoreModel"
import RestResponse from "./models/RestResponse";
import ProductModel from "./models/ProductModel";
import Permission from "./models/Permission";
import MemberModel from "./models/MemberModel";
import { registerModel } from "./models/registerModel";
import cartModel from "./models/CartModel";
import { get } from "http";
import ProductCartModel from "./models/ProductCartModel";
import { OrderModel } from "./models/OrderModel"; // Adjust the path as needed
import axios from "axios";
import RoleModel from "./models/RoleModel";
import CreateStoreModel from "./models/CreateStoreModel";
import { NotificationModel } from "./models/NotificationModel";
import { ProductOrderModel } from "./models/ProductOrderModel";
import ProductDataPrice from "./models/ProductDataPrice";
import StoreRequestModel from "./models/StoreRequestModel";
import PolicyDescriptionModel from "./models/PolicyDescriptionModel";
import AddProductModel from "./models/AddProductModel";
import { BankAccountModel } from "./models/BankAccountModel";
import { PurchaseInfoModel } from "./models/PurchaseInfoModel";
import { configureStore, createSlice } from '@reduxjs/toolkit'
import { AdvancedOrderModel } from "./models/AdvancedOrderModel";


const errorSlice = createSlice({
    name: 'conerror',
    initialState: {
        value: false,
    },
    reducers: {
        setFalse: (state) => {
            state.value = false
        },
        setTrue: (state) => {
            state.value = true
        }
    }
})

export const { setFalse, setTrue } = errorSlice.actions



export const connectionError = configureStore({
    reducer: {
        conerror: errorSlice.reducer
    }
});


const server: string = 'http://127.0.0.1:8080';

export const login = async (username: string, password: string) => {
    ///request REST to login...
    try {
        const response = (await axios.post(`${server}/api/user/login`, { username: username, password: password }, {
            headers: {
                'Content-Type': 'application/json',
            }
        })).data
        if (response.error && response.errorString === "DB Error") {
            connectionError.dispatch(errorSlice.actions.setTrue())
        }
        return response;
    } catch (error) {
        connectionError.dispatch(errorSlice.actions.setTrue())
    }
    return { dataJson: "this will never happen", error: true, errorString: "We couldn't reach the server" };;
}

export const checkAlive = async() => {
    let url = `${server}/api/user/checkGuestExist?guestId=${localStorage.getItem("guestId")}`;
    let headers:any = {
        'Content-Type': 'application/json',
    }
    if(localStorage.getItem("token") != null){
        url = `${server}/api/user/loginUsingJwt?username=${localStorage.getItem("username")}`;
        headers = {
            'Content-Type': 'application/json',
            Authorization: `Bearer ${localStorage.getItem("token")}`
        }
    }
    try {
        const response = (await axios.post(url, {}, {
            headers: headers
        })).data
        if (response.error && response.errorString === "DB Error") {
            return false
        }
        return true;
    } catch (error) {
        return false;
    }
}

export const loginFromGuest = async (username: string, password: string, guestId: number) => {
    ///request REST to login...
    try {
        const response = (await axios.post(`${server}/api/user/loginFromGuest`, { username: username, password: password, guestId: guestId }, {
            headers: {
                'Content-Type': 'application/json',
                //Authorization: `Bearer ${jwt_token}`
            }
        })).data
        if (response.error && response.errorString === "DB Error") {
            connectionError.dispatch(errorSlice.actions.setTrue())
        }
        return response;
    } catch (error) {
        connectionError.dispatch(errorSlice.actions.setTrue())
    }
    return { dataJson: "this will never happen", error: true, errorString: "We couldn't reach the server" };;
}
export const loginUsingJwt = async (username: string, jwt: string) => {
    ///request REST to login...
    try {
        const response = (await axios.post(`${server}/api/user/loginUsingJwt?username=${username}`, {}, {
            headers: {
                'Content-Type': 'application/json',
                Authorization: `Bearer ${jwt}`
            }
        })).data
        if (response.error && response.errorString === "DB Error") {
            connectionError.dispatch(errorSlice.actions.setTrue())
        }
        return response;
    } catch (error) {
        connectionError.dispatch(errorSlice.actions.setTrue())
    }
    return { dataJson: "this will never happen", error: true, errorString: "We couldn't reach the server" };;
}
export const logout = async (username: string) => {
    ///request REST to login...
    try {
        const response = (await axios.post(`${server}/api/user/logout`, { field: username }, {
            headers: {
                'Content-Type': 'application/json',
                Authorization: `Bearer ${localStorage.getItem("token")}`
            }
        })).data
        if (response.error && response.errorString === "DB Error") {
            connectionError.dispatch(errorSlice.actions.setTrue())
        }
        return Number.parseInt(response.dataJson);
    } catch (error) {
        connectionError.dispatch(errorSlice.actions.setTrue())
    }
    return -1;
}
export const enterAsGuest = async () => {
    ///request REST to login...
    try {
        const response = (await axios.post(`${server}/api/user/enterAsGuest`)).data
        console.log(response);
        if (response.error && response.errorString === "DB Error") {
            connectionError.dispatch(errorSlice.actions.setTrue())
        }
        return Number.parseInt(response.dataJson);
    } catch (error) {
        connectionError.dispatch(errorSlice.actions.setTrue())
    }
    return -1;

}
export const registerMember = async (registerModel: registerModel) => {
    ///request REST to login...
    try {
        const response = await (await axios.post(
            `${server}/api/user/register`,
            registerModel,
            {
                headers: {
                    'Content-Type': 'application/json',
                    // Authorization: `Bearer ${jwt_token}` // Uncomment if you have a JWT token
                }
            }
        )).data;
        if (response.error && response.errorString === "DB Error") {
            connectionError.dispatch(errorSlice.actions.setTrue())
        }
        return response;
    } catch (error) {
        connectionError.dispatch(errorSlice.actions.setTrue())
    }
    return { dataJson: "this will never happen", error: true, errorString: "We couldn't reach the server" };;
}

export const getStoreInfo = async (storeId: string): Promise<RestResponse> => {
    let url = `${server}/api/stores/getStoreInfo?username=${localStorage.getItem("username")}&storeId=${storeId}`;
    let headers: any = {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${localStorage.getItem("token")}` // Uncomment if you have a JWT token
    }
    if (localStorage.getItem('token') == null) {
        url = `${server}/api/stores/getStoreInfo?storeId=${storeId}`;
        headers = { 'Content-Type': 'application/json', }
    }
    try {
        const response = await fetch(
            url,
            {
                headers: headers
            }
        );
        const data: RestResponse = await response.json();

        if (data.error && data.errorString === "DB Error") {
            connectionError.dispatch(errorSlice.actions.setTrue())
        }
        return data;
    } catch (error) {
        connectionError.dispatch(errorSlice.actions.setTrue())
    }
    return { dataJson: "this will never happen", error: true, errorString: "We couldn't reach the server" };
}

export const checkIsSystemManager = async (username: string): Promise<boolean> => {
    try {
        const response = await axios.post(
            `${server}/api/user/checkIfSystemManager`,
            null,
            {
                headers: {
                    'Content-Type': 'application/json',
                    Authorization: `Bearer ${localStorage.getItem('token')}`
                },
                params: {
                    username
                }
            }
        );

        const data: RestResponse = await response.data;

        if (data.error && data.errorString === "DB Error") {
            connectionError.dispatch(errorSlice.actions.setTrue())
        }
        return !data.error && data.dataJson === 'true';
    } catch (error) {
        connectionError.dispatch(errorSlice.actions.setTrue())
    }
    return false;
};

export const getStoreDiscounts = async (storeId: string): Promise<PolicyDescriptionModel[]> => {
    let url = `${server}/api/stores/describeStoreDiscountPolicy?username=${localStorage.getItem("username")}&storeId=${storeId}`;
    let headers: any = {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${localStorage.getItem("token")}` // Uncomment if you have a JWT token
    }
    if (localStorage.getItem('token') == null) {
        url = `${server}/api/stores/describeStoreDiscountPolicy?storeId=${storeId}`;
        headers = {
            'Content-Type': 'application/json',
        }
    }
    try {
        const response = await fetch(
            url,
            {
                headers: headers
            }
        );
        const data: RestResponse = await response.json();

        if (data.error && data.errorString === "DB Error") {
            connectionError.dispatch(errorSlice.actions.setTrue())
        }
        if (data.error) {
            return [];
        }
        return JSON.parse(data.dataJson);
    } catch (error) {
        connectionError.dispatch(errorSlice.actions.setTrue())
    }
    return [];
}

export const getStorePolicies = async (storeId: string): Promise<PolicyDescriptionModel[]> => {
    let url = `${server}/api/stores/describeStoreBuyPolicy?username=${localStorage.getItem("username")}&storeId=${storeId}`;
    let headers: any = {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${localStorage.getItem("token")}` // Uncomment if you have a JWT token
    }
    if (localStorage.getItem('token') == null) {
        url = `${server}/api/stores/describeStoreBuyPolicy?storeId=${storeId}`;
        headers = {
            'Content-Type': 'application/json',
        }
    }
    try {
        const response = await fetch(
            url,
            {
                headers: headers
            }
        );
        const data: RestResponse = await response.json();

        if (data.error && data.errorString === "DB Error") {
            connectionError.dispatch(errorSlice.actions.setTrue())
        }
        if (data.error) {
            return [];
        }
        return JSON.parse(data.dataJson);
    } catch (error) {
        connectionError.dispatch(errorSlice.actions.setTrue())
    }
    return [];
}


export const searchAndFilterStoreProducts = async (storeId: string, category: string, keywords: string, minprice: number, maxprice: number): Promise<ProductModel[]> => {
    let request = {
        storeId: parseInt(storeId),
        productName: keywords,
        productPrice: maxprice,
        category: category.toLowerCase() === "all" ? null : category,
        rank: 3
    }
    let url = `${server}/api/stores/getStoreProductsInfo`;
    let headers: any = {
        'Content-Type': 'application/json',
        // Authorization: `Bearer ${jwt_token}`
    }
    if (localStorage.getItem('token') != null) {
        url = `${server}/api/stores/getStoreProductsInfo?username=${localStorage.getItem("username")}`;
        headers = {
            'Content-Type': 'application/json',
            Authorization: `Bearer ${localStorage.getItem("token")}` // Uncomment if you have a JWT token
        }
    }
    try {
        const response = (await axios.patch(url, request,
            {
                headers: headers
            })).data;

        if (response.error && response.errorString === "DB Error") {
            connectionError.dispatch(errorSlice.actions.setTrue())
        }
        if (response.error) {
            return []
        }

        const dataJson = JSON.parse(response.dataJson);

        console.log(dataJson)
        const products: ProductModel[] = Object.entries(dataJson).map(([key, value]) => {
            let product: ProductModel = JSON.parse(key)
            return product
        });


        return products;
    } catch (error) {
        connectionError.dispatch(errorSlice.actions.setTrue())
    }
    return [];
}

export const isOwner = async (storeId: string): Promise<boolean> => {
    try {
        const response = await fetch(
            `${server}/api/stores/isOwner?actorUsername=${localStorage.getItem("username")}&storeId=${storeId}`,
            {
                headers: {
                    'Content-Type': 'application/json',
                    Authorization: `Bearer ${localStorage.getItem("token")}` // Uncomment if you have a JWT token
                }
            }
        );
        const data: RestResponse = await response.json();

        if (data.error && data.errorString === "DB Error") {
            connectionError.dispatch(errorSlice.actions.setTrue())
        }
        return (!data.error) && (data.dataJson === "true")
    } catch (error) {
        connectionError.dispatch(errorSlice.actions.setTrue())
    }
    return false;
}

export const isFounder = async (storeId: string): Promise<boolean> => {
    try {
        const response = await fetch(
            `${server}/api/stores/isFounder?actorUsername=${localStorage.getItem("username")}&storeId=${storeId}`,
            {
                headers: {
                    'Content-Type': 'application/json',
                    Authorization: `Bearer ${localStorage.getItem("token")}` // Uncomment if you have a JWT token
                }
            }
        );
        const data: RestResponse = await response.json();

        if (data.error && data.errorString === "DB Error") {
            connectionError.dispatch(errorSlice.actions.setTrue())
        }
        return (!data.error) && (data.dataJson === "true")
    } catch (error) {
        connectionError.dispatch(errorSlice.actions.setTrue())
    }
    return false;
}

export const isManager = async (storeId: string): Promise<boolean> => {
    try {
        const response = await fetch(
            `${server}/api/stores/isManager?actorUsername=${localStorage.getItem("username")}&storeId=${storeId}`,
            {
                headers: {
                    'Content-Type': 'application/json',
                    Authorization: `Bearer ${localStorage.getItem("token")}` // Uncomment if you have a JWT token
                }
            }
        );
        const data: RestResponse = await response.json();

        if (data.error && data.errorString === "DB Error") {
            connectionError.dispatch(errorSlice.actions.setTrue())
        }
        return (!data.error) && (data.dataJson === "true")
    } catch (error) {
        connectionError.dispatch(errorSlice.actions.setTrue())
    }
    return false;
}

export const hasPermission = async (storeId: string, permission: Permission): Promise<boolean> => {
    try {
        const response = await fetch(
            `${server}/api/stores/hasPermission?actorUsername=${localStorage.getItem("username")}&actionUsername=${localStorage.getItem("username")}&storeId=${storeId}&permission=${permission}`,
            {
                headers: {
                    'Content-Type': 'application/json',
                    Authorization: `Bearer ${localStorage.getItem("token")}` // Uncomment if you have a JWT token
                }
            }
        );
        const data: RestResponse = await response.json();

        if (data.error && data.errorString === "DB Error") {
            connectionError.dispatch(errorSlice.actions.setTrue())
        }
        return (!data.error) && (data.dataJson === "true")
    } catch (error) {
        connectionError.dispatch(errorSlice.actions.setTrue())
    }
    return false;
}

export const storeActive = async (storeId: string): Promise<boolean> => {
    try {
        const response = await fetch(
            `${server}/api/stores/isActive?storeId=${storeId}`,
            {
                headers: {
                    'Content-Type': 'application/json',
                }
            }
        );
        const data: RestResponse = await response.json();

        if (data.error && data.errorString === "DB Error") {
            connectionError.dispatch(errorSlice.actions.setTrue())
        }
        return (!data.error) && (data.dataJson === "true")
    } catch (error) {
        connectionError.dispatch(errorSlice.actions.setTrue())
    }
    return false;
}

export const getPermissions = async (storeId: string): Promise<Permission[]> => {
    try {
        const response = await fetch(
            `${server}/api/stores/getManagerPermissionsInt?currentOwnerUsername=${localStorage.getItem("username")}&managerUsername=${localStorage.getItem("username")}&storeId=${storeId}`,
            {
                headers: {
                    'Content-Type': 'application/json',
                    Authorization: `Bearer ${localStorage.getItem("token")}` // Uncomment if you have a JWT token
                }
            }
        );
        const data: RestResponse = await response.json();

        if (data.error && data.errorString === "DB Error") {
            connectionError.dispatch(errorSlice.actions.setTrue())
        }
        const perms: number[] = JSON.parse(data.dataJson);
        return perms;
    } catch (error) {
        connectionError.dispatch(errorSlice.actions.setTrue())
    }
    return [];
}

export const getMangerPermissions = async (storeId: string, managerUsername: string): Promise<Permission[]> => {
    try {
        const response = await fetch(
            `${server}/api/stores/getManagerPermissionsInt?currentOwnerUsername=${localStorage.getItem("username")}&managerUsername=${managerUsername}&storeId=${storeId}`,
            {
                headers: {
                    'Content-Type': 'application/json',
                    Authorization: `Bearer ${localStorage.getItem("token")}` // Uncomment if you have a JWT token
                }
            }
        );
        const data: RestResponse = await response.json();

        if (data.error && data.errorString === "DB Error") {
            connectionError.dispatch(errorSlice.actions.setTrue())
        }
        const perms: number[] = JSON.parse(data.dataJson);
        return perms;
    } catch (error) {
        connectionError.dispatch(errorSlice.actions.setTrue())
    }
    return [];
}

export const updateManagerPermissions = async (storeId: string, managerUsername: string, perms: Permission[]): Promise<boolean> => {
    let permissionRequest = {
        managerUsername: managerUsername,
        storeId: parseInt(storeId),
        permission: perms
    }
    try {
        const response = await axios.patch(`${server}/api/stores/changeManagerPermission?username=${localStorage.getItem("username")}`, permissionRequest, {
            headers: {
                'Content-Type': 'application/json',
                Authorization: `Bearer ${localStorage.getItem("token")}` // Uncomment if you have a JWT token
            }
        }
        );
        const data = await response.data;

        if (data.error && data.errorString === "DB Error") {
            connectionError.dispatch(errorSlice.actions.setTrue())
        }
        if (data.error) {
            alert(data.errorString)
            return false;
        }
        return true;
    } catch (error) {
        connectionError.dispatch(errorSlice.actions.setTrue())
    }
    return false;
}

export const getStoreManagers = async (storeId: string): Promise<MemberModel[]> => {
    try {
        const response = await fetch(
            `${server}/api/stores/getManagers?username=${localStorage.getItem("username")}&storeId=${storeId}`,
            {
                headers: {
                    'Content-Type': 'application/json',
                    Authorization: `Bearer ${localStorage.getItem("token")}` // Uncomment if you have a JWT token
                }
            }
        );
        const data: RestResponse = await response.json();
        if (data.error && data.errorString === "DB Error") {
            connectionError.dispatch(errorSlice.actions.setTrue())
        }
        return (JSON.parse(data.dataJson) as MemberModel[]);
    } catch (error) {
        connectionError.dispatch(errorSlice.actions.setTrue())
    }
    return [];
}

export const getStoreOwners = async (storeId: string): Promise<MemberModel[]> => {
    try {
        const response = await fetch(
            `${server}/api/stores/getOwners?username=${localStorage.getItem("username")}&storeId=${storeId}`,
            {
                headers: {
                    'Content-Type': 'application/json',
                    Authorization: `Bearer ${localStorage.getItem("token")}` // Uncomment if you have a JWT token
                }
            }
        );
        const data: RestResponse = await response.json();
        if (data.error && data.errorString === "DB Error") {
            connectionError.dispatch(errorSlice.actions.setTrue())
        }
        return (JSON.parse(data.dataJson) as MemberModel[]);
    } catch (error) {
        connectionError.dispatch(errorSlice.actions.setTrue())
    }
    return [];
}

export const acceptRequest = async (requestId: number) => {
    try {
        axios.post(`${server}/api/user/acceptRequest?acceptingName=${localStorage.getItem("username")}&requestID=${requestId}`, {}, {
            headers: {
                'Content-Type': 'application/json',
                Authorization: `Bearer ${localStorage.getItem("token")}`
            }
        })
    } catch (error) {
        connectionError.dispatch(errorSlice.actions.setTrue())
    }
}

export const rejectRequest = async (requestId: number) => {
    try {
        axios.post(`${server}/api/user/rejectRequest?acceptingName=${localStorage.getItem("username")}&requestID=${requestId}`, {}, {
            headers: {
                'Content-Type': 'application/json',
                Authorization: `Bearer ${localStorage.getItem("token")}`
            }
        })
    } catch (error) {
        connectionError.dispatch(errorSlice.actions.setTrue())
    }
}

export const okNotification = async (notifId: number) => {
    try {
        axios.post(`${server}/api/user/okNotification?username=${localStorage.getItem("username")}&notifID=${notifId}`, {}, {
            headers: {
                'Content-Type': 'application/json',
                Authorization: `Bearer ${localStorage.getItem("token")}`
            }
        })
    } catch (error) {
        connectionError.dispatch(errorSlice.actions.setTrue())
    }
}

export const getMember = async (username: string): Promise<MemberModel> => {
    try {
        const response = await fetch(
            `${server}/api/user/getUserDTO?username=${username}`,
            {
                headers: {
                    'Content-Type': 'application/json',
                    Authorization: `Bearer ${localStorage.getItem("token")}` // Uncomment if you have a JWT token
                }
            }
        );
        const data = await response.json();
        if (data.error && data.errorString === "DB Error") {
            connectionError.dispatch(errorSlice.actions.setTrue())
        }
        // Assuming the API returns the data in dataJson
        const profileData = JSON.parse(data.dataJson) as MemberModel;
        // Validate the structure of profileData before returning

        return profileData;
    } catch (error) {
        connectionError.dispatch(errorSlice.actions.setTrue())
    }
    return { username: "this will never happen", firstName: "never", lastName: "never", emailAddress: "never", phoneNumber: "never", birthDate: "never" }
}

export const viewMemberCart = async (username: string): Promise<string> => {
    try {
        const response = await fetch(`${server}/api/user/viewCart?username=${username}`, {
            headers: {
                'Content-Type': 'application/json',
                Authorization: `Bearer ${localStorage.getItem("token")}` // Uncomment if you have a JWT token
            }
        })
        const data = await response.json();
        if (data.error && data.errorString === "DB Error") {
            connectionError.dispatch(errorSlice.actions.setTrue())
        }
        return data;
    } catch (error) {
        connectionError.dispatch(errorSlice.actions.setTrue())
    }
    return "";
}

export const viewGuestCart = async (guestId: number): Promise<any> => {
    try {
        const response = await fetch(`${server}/api/user/guest/viewCart?guestId=${guestId}`);
        const data = await response.json();
        if (data.error && data.errorString === "DB Error") {
            connectionError.dispatch(errorSlice.actions.setTrue())
        }
        return data;
    } catch (error) {
        connectionError.dispatch(errorSlice.actions.setTrue())
    }
    return "";
}

export const getProductDetails = async (productId: number): Promise<RestResponse> => {
    let url = `${server}/api/stores/getProductInfo?username=${localStorage.getItem("username")}&productId=${productId}`;
    let headers: any = {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${localStorage.getItem("token")}` // Uncomment if you have a JWT token
    }
    if (localStorage.getItem('token') == null) {
        url = `${server}/api/stores/getProductInfo?productId=${productId}`;
        headers = {
            'Content-Type': 'application/json',
        }
    }

    try {
        const response = await fetch(
            url,
            {
                headers: headers
            }
        );
        const data: RestResponse = await response.json();
        if (data.error && data.errorString === "DB Error") {
            connectionError.dispatch(errorSlice.actions.setTrue())
        }
        return data;
    } catch (error) {
        connectionError.dispatch(errorSlice.actions.setTrue())
    }
    return { dataJson: "this will never happen", error: true, errorString: "We couldn't reach the server" };
}

export const getProductAmount = async (productId: number, storeId: number): Promise<number> => {
    let request = {
        storeId: storeId,
        productId: productId
    }
    let url = `${server}/api/stores/getStoreProductAmount`;
    let headers: any = {
        'Content-Type': 'application/json',
        // Authorization: `Bearer ${jwt_token}`
    }
    if (localStorage.getItem('token') != null) {
        url = `${server}/api/stores/getStoreProductAmount?username=${localStorage.getItem("username")}`;
        headers = {
            'Content-Type': 'application/json',
            Authorization: `Bearer ${localStorage.getItem("token")}` // Uncomment if you have a JWT token
        }
    }
    try {
        const response = (await axios.patch(url, request,
            {
                headers: headers
            })).data;

        if (response.error && response.errorString === "DB Error") {
            connectionError.dispatch(errorSlice.actions.setTrue())
        }
        if (response.error) {
            alert(response.errorString);
            return 0;
        }

        const amount = parseInt(response.dataJson);

        console.log(amount)
        return amount;
    } catch (error) {
        connectionError.dispatch(errorSlice.actions.setTrue())
    }
    return 0;
}

export const updateFirstName = async (firstName: string) => {
    try {
        const response = await (await axios.patch(
            `${server}/api/user/setFirstName?username=${localStorage.getItem("username")}`,
            { field: firstName },
            {
                headers: {
                    'Content-Type': 'application/json',
                    Authorization: `Bearer ${localStorage.getItem("token")}` // Uncomment if you have a JWT token
                }
            }
        )).data;
        if (response.error && response.errorString === "DB Error") {
            connectionError.dispatch(errorSlice.actions.setTrue())
        }
        return response;
    } catch (error) {
        connectionError.dispatch(errorSlice.actions.setTrue())
    }
    return { dataJson: "this will never happen", error: true, errorString: "We couldn't reach the server" };;
}
export const updateLastName = async (lastName: string) => {
    try {
        const response = await (await axios.patch(
            `${server}/api/user/setLastName?username=${localStorage.getItem("username")}`,
            { field: lastName },
            {
                headers: {
                    'Content-Type': 'application/json',
                    Authorization: `Bearer ${localStorage.getItem("token")}` // Uncomment if you have a JWT token
                }
            }
        )).data;
        if (response.error && response.errorString === "DB Error") {
            connectionError.dispatch(errorSlice.actions.setTrue())
        }
        return response;
    } catch (error) {
        connectionError.dispatch(errorSlice.actions.setTrue())
    }
    return { dataJson: "this will never happen", error: true, errorString: "We couldn't reach the server" };;
}
export const updateEmail = async (email: string) => {
    try {
        const response = await (await axios.patch(
            `${server}/api/user/setEmailAddress?username=${localStorage.getItem("username")}`,
            { field: email },
            {
                headers: {
                    'Content-Type': 'application/json',
                    Authorization: `Bearer ${localStorage.getItem("token")}` // Uncomment if you have a JWT token
                }
            }
        )).data;
        if (response.error && response.errorString === "DB Error") {
            connectionError.dispatch(errorSlice.actions.setTrue())
        }
        return response;
    } catch (error) {
        connectionError.dispatch(errorSlice.actions.setTrue())
    }
    return { dataJson: "this will never happen", error: true, errorString: "We couldn't reach the server" };;
}
export const updatePhone = async (phone: string) => {
    try {
        const response = await (await axios.patch(
            `${server}/api/user/setPhoneNumber?username=${localStorage.getItem("username")}`,
            { field: phone },
            {
                headers: {
                    'Content-Type': 'application/json',
                    Authorization: `Bearer ${localStorage.getItem("token")}` // Uncomment if you have a JWT token
                }
            }
        )).data;
        if (response.error && response.errorString === "DB Error") {
            connectionError.dispatch(errorSlice.actions.setTrue())
        }
        return response;
    } catch (error) {
        connectionError.dispatch(errorSlice.actions.setTrue())
    }
    return { dataJson: "this will never happen", error: true, errorString: "We couldn't reach the server" };;
}
export const updateBirthday = async (birthday: string) => {
    try {
        const response = await (await axios.patch(
            `${server}/api/user/setBirthday?username=${localStorage.getItem("username")}`,
            { field: birthday },
            {
                headers: {
                    'Content-Type': 'application/json',
                    Authorization: `Bearer ${localStorage.getItem("token")}` // Uncomment if you have a JWT token
                }
            }
        )).data;
        if (response.error && response.errorString === "DB Error") {
            connectionError.dispatch(errorSlice.actions.setTrue())
        }
        return response;
    } catch (error) {
        connectionError.dispatch(errorSlice.actions.setTrue())
    }
    return { dataJson: "this will never happen", error: true, errorString: "We couldn't reach the server" };;
}

export const searchProducts = async (
    username: string,
    productName: string,
    productCategory: string,
    minProductPrice: number,
    maxProductPrice: number,
    minProductRank: number
): Promise<ProductModel[]> => {

    try {
        const response = (await axios.get(`${server}/api/product/getFilteredProducts`, {
            headers: {
                'Content-Type': 'application/json',
                // Authorization: `Bearer ${jwt_token}`
            },
            params: {
                productName,
                productCategory: (productCategory === "Other" ? "all" : productCategory),
                minProductPrice,
                maxProductPrice,
                minProductRank
            }
        })).data;
        if (response.error && response.errorString === "DB Error") {
            connectionError.dispatch(errorSlice.actions.setTrue())
        }
        if(response.error){
            alert(response.errorString);
            return [];
        }
        const dataJson = JSON.parse(response.dataJson);
        const products: ProductModel[] = dataJson.map((product: any) => ({
            productID: product.productID,
            productName: product.productName,
            storeId: product.storeId, // Assuming storeId is same as productID
            productPrice: product.productPrice,
            productCategory: product.productCategory,
            productDescription: product.description,
            productRank: product.productRank
        }));


        return products;
    } catch (error) {
        connectionError.dispatch(errorSlice.actions.setTrue())
    }
    return [];
};



export const getOrders = async (username: string): Promise<OrderModel[]> => {
    try {
        const response = (await axios.get(`${server}/api/user/getOrderHistory?username=${username}`, {
            headers: {
                'Content-Type': 'application/json',
                Authorization: `Bearer ${localStorage.getItem("token")}`
            }
        })).data;

        if(response.error){
            alert(response.errorString);
            return [];
        }

        const ordersData: { [key: number]: { products: ProductDataPrice[], dateTimeOfPurchase: string } } = JSON.parse(response.dataJson);

        const orders: OrderModel[] = Object.entries(ordersData).map(([orderId, orderDetails]) => {
            const { products, dateTimeOfPurchase } = orderDetails;

            const total = products.reduce((acc, product) => acc + product.newPrice * product.amount, 0);
            const orderProducts: ProductOrderModel[] = products.map(product => ({
                id: product.id,
                name: product.name,
                quantity: product.amount,
                storeId: product.storeId,
                oldPrice: product.oldPrice,
                newPrice: product.newPrice
            }));

            const date = new Date(dateTimeOfPurchase).toLocaleDateString();

            return {
                id: orderId,
                date: date,
                total: total,
                products: orderProducts
            };
        });

        return orders;

    } catch (error) {
        console.error("Failed to fetch orders:", error);
        return [];
    }
};

export const getStoreOrderHistory = async (storeId: string): Promise<AdvancedOrderModel[]> => {
    try {

        const response = (await axios.get(`${server}/api/stores/getStoreOrderHistory?username=${localStorage.getItem("username")}&storeId=${storeId}`, {
            headers: {
                'Content-Type': 'application/json',
                Authorization: `Bearer ${localStorage.getItem("token")}`
            }
        })).data;

        if(response.error){
            alert(response.errorString);
            return [];
        }

        const ordersData: AdvancedOrderModel[] = JSON.parse(response.dataJson);
        console.log(ordersData);
        ordersData.forEach(order => {
            order.products = Object.entries(order.orderProductsJsons).map(([key,value]) => JSON.parse(value))
        })
        return ordersData;


    } catch (error) {
        console.error("Failed to fetch orders:", error);
        return [];
    }
};

export const fetchUserStores = async (username: string): Promise<RoleModel[]> => {
    try {
        const response = await fetch(`${server}/api/user/getUserRoles?username=${username}`, {
            headers: {
                'Content-Type': 'application/json',
                Authorization: `Bearer ${localStorage.getItem("token")}` // Uncomment if you have a JWT token
            }
        });
        const res = await response.json();
        if (res.error && res.errorString === "DB Error") {
            connectionError.dispatch(errorSlice.actions.setTrue())
        }
        const stores = JSON.parse(res.dataJson) as RoleModel[];
        return stores;
    } catch (error) {
        connectionError.dispatch(errorSlice.actions.setTrue())
    }
    return [];
};

export const fetchNotifications = async (): Promise<NotificationModel[]> => {
    try {
        const response = await fetch(
            `${server}/api/user/getNotifications?username=${localStorage.getItem("username")}`,
            {
                headers: {
                    'Content-Type': 'application/json',
                    Authorization: `Bearer ${localStorage.getItem("token")}` // Uncomment if you have a JWT token
                }
            }
        );
        const data: RestResponse = await response.json();
        if (data.error && data.errorString === "DB Error") {
            connectionError.dispatch(errorSlice.actions.setTrue())
        }
        if (data.error) {
            return [];
        }
        // Assuming the API returns the data in dataJson
        const notifs = JSON.parse(data.dataJson);
        console.log(data.dataJson)
        return notifs
    } catch (error) {
        connectionError.dispatch(errorSlice.actions.setTrue())
    }
    return [];
};

export const createNewStore = async (storeModel: CreateStoreModel, storeFounder: string = localStorage.getItem("username") as string) => {
    storeModel.founderUsername = storeFounder;
    try {
        const response = (await axios.post(`${server}/api/stores/createStore`, storeModel, {
            headers: {
                'Content-Type': 'application/json',
                Authorization: `Bearer ${localStorage.getItem("token")}` // Uncomment if you have a JWT token
            }
        }
        )).data;
        if (response.error && response.errorString === "DB Error") {
            connectionError.dispatch(errorSlice.actions.setTrue())
        }
        return response;
    } catch (error) {
        connectionError.dispatch(errorSlice.actions.setTrue())
    }
    return { dataJson: "this will never happen", error: true, errorString: "We couldn't reach the server" };;
};

export const addProduct = async (productModel: AddProductModel, storeId: number) => {
    productModel.storeId = storeId;
    productModel.rank = 3;
    try {
        const response = (await axios.post(`${server}/api/stores/addProductToStore?username=${localStorage.getItem("username")}`, productModel, {
            headers: {
                'Content-Type': 'application/json',
                Authorization: `Bearer ${localStorage.getItem("token")}` // Uncomment if you have a JWT token
            }
        }
        )).data;
        if (response.error && response.errorString === "DB Error") {
            connectionError.dispatch(errorSlice.actions.setTrue())
        }
        console.log(response);
        return response;
    } catch (error) {
        connectionError.dispatch(errorSlice.actions.setTrue())
    }
    return { dataJson: "this will never happen", error: true, errorString: "We couldn't reach the server" };;
};

export const editProduct = async (productModel: AddProductModel, storeId: number, productId: number) => {
    productModel.storeId = storeId;
    productModel.rank = 3;
    productModel.productId = productId;
    try {
        const response = (await axios.put(`${server}/api/stores/updateProductInStore?username=${localStorage.getItem("username")}`, productModel, {
            headers: {
                'Content-Type': 'application/json',
                Authorization: `Bearer ${localStorage.getItem("token")}` // Uncomment if you have a JWT token
            }
        }
        )).data;
        if (response.error && response.errorString === "DB Error") {
            connectionError.dispatch(errorSlice.actions.setTrue())
        }
        return response;
    } catch (error) {
        connectionError.dispatch(errorSlice.actions.setTrue())
    }
    return { dataJson: "this will never happen", error: true, errorString: "We couldn't reach the server" };;
};

export const closeStore = async (storeId: string) => {
    try {
        const response = await axios.put(`${server}/api/stores/closeStore?username=${localStorage.getItem("username")}&storeId=${storeId}`, {}, {
            headers: {
                'Content-Type': 'application/json',
                Authorization: `Bearer ${localStorage.getItem("token")}` // Uncomment if you have a JWT token
            }
        }
        );
        const data: RestResponse = response.data;
        if (data.error && data.errorString === "DB Error") {
            connectionError.dispatch(errorSlice.actions.setTrue())
        }
        if (data.error) {
            alert(`Couldn't close store: ${data.errorString}`);
        } else {
            alert(`Store is now closed :(`)
        }
    } catch (error) {
        connectionError.dispatch(errorSlice.actions.setTrue())
    }
}

export const reopenStore = async (storeId: string) => {
    try {
        const response = await axios.put(`${server}/api/stores/reopenStore?username=${localStorage.getItem("username")}&storeId=${storeId}`, {}, {
            headers: {
                'Content-Type': 'application/json',
                Authorization: `Bearer ${localStorage.getItem("token")}` // Uncomment if you have a JWT token
            }
        }
        );
        const data: RestResponse = response.data;
        if (data.error && data.errorString === "DB Error") {
            connectionError.dispatch(errorSlice.actions.setTrue())
        }
        if (data.error) {
            alert(`Couldn't reopen store: ${data.errorString}`);
        } else {
            alert(`Store is now open! :D`)
        }
    } catch (error) {
        connectionError.dispatch(errorSlice.actions.setTrue())
    }
}

export const sendManagerRequest = async (username: string, storeId: string): Promise<RestResponse> => {
    let request: StoreRequestModel = { appointer: localStorage.getItem("username")!, appointee: username, storeId: parseInt(storeId) }
    try {
        const response = (await axios.post(`${server}/api/stores/sendStoreManagerRequest`, request, {
            headers: {
                'Content-Type': 'application/json',
                Authorization: `Bearer ${localStorage.getItem("token")}` // Uncomment if you have a JWT token
            }
        }
        )).data;
        if (response.error && response.errorString === "DB Error") {
            connectionError.dispatch(errorSlice.actions.setTrue())
        }
        return response;
    } catch (error) {
        connectionError.dispatch(errorSlice.actions.setTrue())
    }
    return { dataJson: "this will never happen", error: true, errorString: "We couldn't reach the server" };
}

export const addProductToCartMember = async (productId: number, storeId: number, amount: number) => {
    try {
        const response = (await axios.patch(`${server}/api/user/addProductToCart?username=${localStorage.getItem("username")}`, { storeId: storeId, productId: productId, amount: amount }, {
            headers: {
                'Content-Type': 'application/json',
                Authorization: `Bearer ${localStorage.getItem("token")}` // Uncomment if you have a JWT token
            }
        }
        )).data;
        if (response.error && response.errorString === "DB Error") {
            connectionError.dispatch(errorSlice.actions.setTrue())
        }
        return response;
    } catch (error) {
        connectionError.dispatch(errorSlice.actions.setTrue())
    }
    return { dataJson: "this will never happen", error: true, errorString: "We couldn't reach the server" };
}

export const sendOwnerRequest = async (username: string, storeId: string): Promise<RestResponse> => {
    let request: StoreRequestModel = { appointer: localStorage.getItem("username")!, appointee: username, storeId: parseInt(storeId) }
    try {
        const response = (await axios.post(`${server}/api/stores/sendStoreOwnerRequest`, request, {
            headers: {
                'Content-Type': 'application/json',
                Authorization: `Bearer ${localStorage.getItem("token")}` // Uncomment if you have a JWT token
            }
        }
        )).data;
        if (response.error && response.errorString === "DB Error") {
            connectionError.dispatch(errorSlice.actions.setTrue())
        }
        return response;
    } catch (error) {
        connectionError.dispatch(errorSlice.actions.setTrue())
    }
    return { dataJson: "this will never happen", error: true, errorString: "We couldn't reach the server" };
}

export const addProductToCartGuest = async (productId: number, storeId: number, amount: number) => {
    try {
        const response = (await axios.patch(`${server}/api/user/guest/addProductToCart?guestId=${localStorage.getItem("guestId")}`, { storeId: storeId, productId: productId, amount: amount })).data;
        if (response.error && response.errorString === "DB Error") {
            connectionError.dispatch(errorSlice.actions.setTrue())
        }
        return response;
    } catch (error) {
        connectionError.dispatch(errorSlice.actions.setTrue())
    }
    return { dataJson: "this will never happen", error: true, errorString: "We couldn't reach the server" };
}

export const changeProductAmountInCart = async (productId: number, storeId: number, amount: number) => {
    try {
        const response = (await axios.patch(`${server}/api/user/changeQuantityCart?username=${localStorage.getItem("username")}`, { storeId: storeId, productId: productId, amount: amount, owner: localStorage.getItem("username") as string }, {
            headers: {
                'Content-Type': 'application/json',
                Authorization: `Bearer ${localStorage.getItem("token")}` // Uncomment if you have a JWT token
            }
        }
        )).data;
        if (response.error && response.errorString === "DB Error") {
            connectionError.dispatch(errorSlice.actions.setTrue())
        }
        return response;
    } catch (error) {
        connectionError.dispatch(errorSlice.actions.setTrue())
    }
    return { dataJson: "this will never happen", error: true, errorString: "We couldn't reach the server" };
}


export const describeDiscountPolicy = async (policyId: string): Promise<RestResponse> => {
    try {
        const response = await (await fetch(
            `${server}/api/stores/describeDiscountPolicy?username=${localStorage.getItem("username")}&policyId=${policyId}`,
            {
                headers: {
                    'Content-Type': 'application/json',
                    Authorization: `Bearer ${localStorage.getItem("token")}` // Uncomment if you have a JWT token
                }
            }
        )).json();
        if (response.error && response.errorString === "DB Error") {
            connectionError.dispatch(errorSlice.actions.setTrue())
        }
        return response;
    } catch (error) {
        connectionError.dispatch(errorSlice.actions.setTrue())
    }
    return { dataJson: "this will never happen", error: true, errorString: "We couldn't reach the server" };
}

export const describeBuyPolicy = async (policyId: string): Promise<RestResponse> => {
    try {
        const response = await (await fetch(
            `${server}/api/stores/describeBuyPolicy?username=${localStorage.getItem("username")}&policyId=${policyId}`,
            {
                headers: {
                    'Content-Type': 'application/json',
                    Authorization: `Bearer ${localStorage.getItem("token")}` // Uncomment if you have a JWT token
                }
            }
        )).json();
        if (response.error && response.errorString === "DB Error") {
            connectionError.dispatch(errorSlice.actions.setTrue())
        }
        return response;
    } catch (error) {
        connectionError.dispatch(errorSlice.actions.setTrue())
    }
    return { dataJson: "this will never happen", error: true, errorString: "We couldn't reach the server" };
}

export const describeCondition = async (policyId: string): Promise<RestResponse> => {
    try {
        const response = await (await fetch(
            `${server}/api/stores/describeDiscountCondition?username=${localStorage.getItem("username")}&condId=${policyId}`,
            {
                headers: {
                    'Content-Type': 'application/json',
                    Authorization: `Bearer ${localStorage.getItem("token")}` // Uncomment if you have a JWT token
                }
            }
        )).json();
        if (response.error && response.errorString === "DB Error") {
            connectionError.dispatch(errorSlice.actions.setTrue())
        }
        return response;
    } catch (error) {
        connectionError.dispatch(errorSlice.actions.setTrue())
    }
    return { dataJson: "this will never happen", error: true, errorString: "We couldn't reach the server" };
}

export const createSimpleDiscount = async (percentage: number, applyOn: string, productId: number, categoryName: string): Promise<string> => {
    let request: any = {
        conditionAID: 0,
        percentage: percentage
    }
    let url = `${server}/api/stores/createOnStoreSimpleDiscountPolicy?username=${localStorage.getItem("username")}`;
    if (applyOn === "produ") {
        url = `${server}/api/stores/createOnProductSimpleDiscountPolicy?username=${localStorage.getItem("username")}`;
        request.productId = productId;
    } else if (applyOn === "categ") {
        request.categoryName = categoryName
        url = `${server}/api/stores/createOnCategorySimpleDiscountPolicy?username=${localStorage.getItem("username")}`;
    }
    try {
        const response = await axios.post(url, request, {
            headers: {
                'Content-Type': 'application/json',
                Authorization: `Bearer ${localStorage.getItem("token")}` // Uncomment if you have a JWT token
            }
        }
        );
        let data: RestResponse = await response.data;
        if (data.error && data.errorString === "DB Error") {
            connectionError.dispatch(errorSlice.actions.setTrue())
        }
        return data.dataJson
    } catch (error) {
        connectionError.dispatch(errorSlice.actions.setTrue())
    }
    return "-1";
}

export const createConditionDiscount = async (conditionId: number, percentage: number, applyOn: string, productId: number, categoryName: string): Promise<string> => {
    let request: any = {
        conditionAID: conditionId,
        percentage: percentage
    }
    let url = `${server}/api/stores/createOnStoreConditionDiscountPolicy?username=${localStorage.getItem("username")}`;
    if (applyOn === "produ") {
        url = `${server}/api/stores/createOnProductConditionDiscountPolicy?username=${localStorage.getItem("username")}`;
        request.productId = productId;
    } else if (applyOn === "categ") {
        request.categoryName = categoryName
        url = `${server}/api/stores/createOnCategoryConditionDiscountPolicy?username=${localStorage.getItem("username")}`;
    }
    try {
        const response = await axios.post(url, request, {
            headers: {
                'Content-Type': 'application/json',
                Authorization: `Bearer ${localStorage.getItem("token")}` // Uncomment if you have a JWT token
            }
        }
        );
        let data: RestResponse = await response.data;
        if (data.error && data.errorString === "DB Error") {
            connectionError.dispatch(errorSlice.actions.setTrue())
        }
        return data.dataJson
    } catch (error) {
        connectionError.dispatch(errorSlice.actions.setTrue())
    }
    return "-1";
}

export const createMinAmountCondition = async (minAmount: number, applyOn: string, productId: number, categoryName: string): Promise<string> => {
    let request: any = {}
    let url = `${server}/api/stores/createMinProductOnStoreCondition?username=${localStorage.getItem("username")}&minAmount=${minAmount}`;
    if (applyOn === "produ") {
        url = `${server}/api/stores/createMinProductCondition?username=${localStorage.getItem("username")}`;
        request = {
            minAmount: minAmount,
            productId: productId
        }
    } else if (applyOn === "categ") {
        request = {
            minAmount: minAmount,
            categoryName: categoryName
        }
        url = `${server}/api/stores/createMinProductOnCategoryCondition?username=${localStorage.getItem("username")}`;
    }
    try {
        const response = await axios.post(url, request, {
            headers: {
                'Content-Type': 'application/json',
                Authorization: `Bearer ${localStorage.getItem("token")}` // Uncomment if you have a JWT token
            }
        }
        );
        let data: RestResponse = await response.data;
        if (data.error && data.errorString === "DB Error") {
            connectionError.dispatch(errorSlice.actions.setTrue())
        }
        return data.dataJson
    } catch (error) {
        connectionError.dispatch(errorSlice.actions.setTrue())
    }
    return "-1";
}

export const createMinBuyCondition = async (minBuy: number): Promise<string> => {
    try {
        const response = await axios.post(`${server}/api/stores/createMinBuyCondition?username=${localStorage.getItem("username")}&minBuy=${minBuy}`, {}, {
            headers: {
                'Content-Type': 'application/json',
                Authorization: `Bearer ${localStorage.getItem("token")}` // Uncomment if you have a JWT token
            }
        }
        );
        let data: RestResponse = await response.data;
        if (data.error && data.errorString === "DB Error") {
            connectionError.dispatch(errorSlice.actions.setTrue())
        }
        return data.dataJson
    } catch (error) {
        connectionError.dispatch(errorSlice.actions.setTrue())
    }
    return "-1";
}

export const createCompositeCondition = async (id1: number, id2: number, logic: string): Promise<string> => {
    let type: string = logic.charAt(0) + logic.substring(1).toLowerCase();
    let request = {
        conditionAID: id1,
        conditionBID: id2
    }
    try {
        const response = await axios.post(`${server}/api/stores/create${type}Condition?username=${localStorage.getItem("username")}`, request, {
            headers: {
                'Content-Type': 'application/json',
                Authorization: `Bearer ${localStorage.getItem("token")}` // Uncomment if you have a JWT token
            }
        }
        );
        let data: RestResponse = await response.data;
        if (data.error && data.errorString === "DB Error") {
            connectionError.dispatch(errorSlice.actions.setTrue())
        }
        if(data.error){
            alert(`Error creating composite condition: ${data.errorString}`)
        }
        return data.dataJson
    } catch (error) {
        connectionError.dispatch(errorSlice.actions.setTrue())
    }
    return "-1";
}

export const createCompositeDiscount = async (id1: number, id2: number, logic: string, minMax: string): Promise<string> => {
    let type: string = logic.charAt(0) + logic.substring(1).toLowerCase();
    let desc: string = minMax.charAt(0) + minMax.substring(1).toLowerCase();
    let request = {
        policyId1: id1,
        policyId2: id2
    }
    try {
        const response = await axios.post(`${server}/api/stores/create${type === 'Xor' ? desc : ''}${type}DiscountPolicy?username=${localStorage.getItem("username")}`, request, {
            headers: {
                'Content-Type': 'application/json',
                Authorization: `Bearer ${localStorage.getItem("token")}` // Uncomment if you have a JWT token
            }
        }
        );
        let data: RestResponse = await response.data;
        if (data.error && data.errorString === "DB Error") {
            connectionError.dispatch(errorSlice.actions.setTrue())
        }
        if(data.error){
            alert(`Error creating composite discount: ${data.errorString}`)
        }
        return data.dataJson
    } catch (error) {
        connectionError.dispatch(errorSlice.actions.setTrue())
    }
    return "-1";
}

export const createCompositePolicy = async (id1: number, id2: number, logic: string): Promise<string> => {
    let type: string = logic.charAt(0) + logic.substring(1).toLowerCase();
    let request = {
        policyId1: id1,
        policyId2: id2
    }
    try {
        const response = await axios.post(`${server}/api/stores/create${type}BuyPolicy?username=${localStorage.getItem("username")}`, request, {
            headers: {
                'Content-Type': 'application/json',
                Authorization: `Bearer ${localStorage.getItem("token")}` // Uncomment if you have a JWT token
            }
        }
        );
        let data: RestResponse = await response.data;
        if (data.error && data.errorString === "DB Error") {
            connectionError.dispatch(errorSlice.actions.setTrue())
        }
        if(data.error){
            alert(`Error creating composite policy: ${data.errorString}`)
        }
        return data.dataJson
    } catch (error) {
        connectionError.dispatch(errorSlice.actions.setTrue())
    }
    return "-1";
}

export const createKgPolicy = async (productId: number, maxWeight: number, minWeight: number): Promise<string> => {
    let request = {
        productId: productId,
        minWeight: minWeight,
        maxWeight: maxWeight,
        buyTypes: []
    }
    try {
        const response = await axios.post(`${server}/api/stores/createProductKgBuyPolicy?username=${localStorage.getItem("username")}`, request, {
            headers: {
                'Content-Type': 'application/json',
                Authorization: `Bearer ${localStorage.getItem("token")}` // Uncomment if you have a JWT token
            }
        }
        );
        let data: RestResponse = await response.data;
        if (data.error && data.errorString === "DB Error") {
            connectionError.dispatch(errorSlice.actions.setTrue())
        }
        if(data.error){
            alert(`Error creating policy: ${data.errorString}`)
        }
        return data.dataJson
    } catch (error) {
        connectionError.dispatch(errorSlice.actions.setTrue())
    }
    return "-1";
}

export const createAmountPolicy = async (productId: number, minAmount: number, maxAmount: number): Promise<string> => {
    let request = {
        productId: productId,
        minAmount: minAmount,
        maxAmount: maxAmount,
        buyTypes: []
    }
    try {
        const response = await axios.post(`${server}/api/stores/createProductAmountBuyPolicy?username=${localStorage.getItem("username")}`, request, {
            headers: {
                'Content-Type': 'application/json',
                Authorization: `Bearer ${localStorage.getItem("token")}` // Uncomment if you have a JWT token
            }
        }
        );
        let data: RestResponse = await response.data;
        if (data.error && data.errorString === "DB Error") {
            connectionError.dispatch(errorSlice.actions.setTrue())
        }
        if(data.error){
            alert(`Error creating policy: ${data.errorString}`)
        }
        return data.dataJson
    } catch (error) {
        connectionError.dispatch(errorSlice.actions.setTrue())
    }
    return "-1";
}

export const createAgePolicy = async (category: string, minAge: number, maxAge: number): Promise<string> => {
    let request = {
        category: category,
        minAge: minAge,
        maxAge: maxAge,
        buyTypes: []
    }
    try {
        const response = await axios.post(`${server}/api/stores/createCategoryAgeLimitBuyPolicy?username=${localStorage.getItem("username")}`, request, {
            headers: {
                'Content-Type': 'application/json',
                Authorization: `Bearer ${localStorage.getItem("token")}` // Uncomment if you have a JWT token
            }
        }
        );
        let data: RestResponse = await response.data;
        if (data.error && data.errorString === "DB Error") {
            connectionError.dispatch(errorSlice.actions.setTrue())
        }
        if(data.error){
            alert(`Error creating policy: ${data.errorString}`)
        }
        return data.dataJson
    } catch (error) {
        connectionError.dispatch(errorSlice.actions.setTrue())
    }
    return "-1";
}

export const createHourPolicy = async (category: string, fromHour: number, fromMinute: number, toHour: number, toMinute: number): Promise<string> => {
    let request = {
        category: category,
        fromHour: [fromHour, fromMinute, 0, 0],
        toHour: [toHour, toMinute, 0, 0,],
        buyTypes: []
    }
    try {
        const response = await axios.post(`${server}/api/stores/createCategoryHourLimitBuyPolicy?username=${localStorage.getItem("username")}`, request, {
            headers: {
                'Content-Type': 'application/json',
                Authorization: `Bearer ${localStorage.getItem("token")}` // Uncomment if you have a JWT token
            }
        }
        );
        let data: RestResponse = await response.data;
        if (data.error && data.errorString === "DB Error") {
            connectionError.dispatch(errorSlice.actions.setTrue())
        }
        if(data.error){
            alert(`Error creating policy: ${data.errorString}`)
        }
        return data.dataJson
    } catch (error) {
        connectionError.dispatch(errorSlice.actions.setTrue())
    }
    return "-1";
}

export const createMonthPolicy = async (category: string): Promise<string> => {
    let request = {
        category: category,
        buyTypes: []
    }
    try {
        const response = await axios.post(`${server}/api/stores/createCategoryRoshChodeshBuyPolicy?username=${localStorage.getItem("username")}`, request, {
            headers: {
                'Content-Type': 'application/json',
                Authorization: `Bearer ${localStorage.getItem("token")}` // Uncomment if you have a JWT token
            }
        }
        );
        let data: RestResponse = await response.data;
        if (data.error && data.errorString === "DB Error") {
            connectionError.dispatch(errorSlice.actions.setTrue())
        }
        return data.dataJson
    } catch (error) {
        connectionError.dispatch(errorSlice.actions.setTrue())
    }
    return "-1";
}

export const createHolidayPolicy = async (category: string): Promise<string> => {
    let request = {
        category: category,
        buyTypes: []
    }
    try {
        const response = await axios.post(`${server}/api/stores/createCategoryHolidayBuyPolicy?username=${localStorage.getItem("username")}`, request, {
            headers: {
                'Content-Type': 'application/json',
                Authorization: `Bearer ${localStorage.getItem("token")}` // Uncomment if you have a JWT token
            }
        }
        );
        let data: RestResponse = await response.data;
        if (data.error && data.errorString === "DB Error") {
            connectionError.dispatch(errorSlice.actions.setTrue())
        }
        return data.dataJson
    } catch (error) {
        connectionError.dispatch(errorSlice.actions.setTrue())
    }
    return "-1";
}

export const createDatePolicy = async (category: string, day: number, month: number, year: number): Promise<string> => {
    let request = {
        category: category,
        day: day,
        month: month,
        year: year,
        buyTypes: []
    }
    try {
        const response = await axios.post(`${server}/api/stores/createCategorySpecificDateBuyPolicy?username=${localStorage.getItem("username")}`, request, {
            headers: {
                'Content-Type': 'application/json',
                Authorization: `Bearer ${localStorage.getItem("token")}` // Uncomment if you have a JWT token
            }
        }
        );
        let data: RestResponse = await response.data;
        if (data.error && data.errorString === "DB Error") {
            connectionError.dispatch(errorSlice.actions.setTrue())
        }
        if(data.error){
            alert(`Error creating policy: ${data.errorString}`)
        }
        return data.dataJson
    } catch (error) {
        connectionError.dispatch(errorSlice.actions.setTrue())
    }
    return "-1";
}

export const addPolicyToStore = async (storeId: number, policyId: number): Promise<string> => {
    let request = {
        storeId: storeId,
        policyId1: policyId
    }
    try {
        const response = await axios.post(`${server}/api/stores/addBuyPolicyToStore?username=${localStorage.getItem("username")}`, request, {
            headers: {
                'Content-Type': 'application/json',
                Authorization: `Bearer ${localStorage.getItem("token")}` // Uncomment if you have a JWT token
            }
        }
        );
        let data: RestResponse = await response.data;
        if (data.error && data.errorString === "DB Error") {
            connectionError.dispatch(errorSlice.actions.setTrue())
        }
        return data.dataJson
    } catch (error) {
        connectionError.dispatch(errorSlice.actions.setTrue())
    }
    return "-1";
}


export const removePolicyFromStore = async (storeId: number, policyId: number): Promise<string> => {
    let request = {
        storeId: storeId,
        policyId1: policyId
    }
    try {
        const response = await axios.patch(`${server}/api/stores/removeBuyPolicyFromStore?username=${localStorage.getItem("username")}`, request, {
            headers: {
                'Content-Type': 'application/json',
                Authorization: `Bearer ${localStorage.getItem("token")}` // Uncomment if you have a JWT token
            }
        }
        );
        let data: RestResponse = await response.data;
        if (data.error && data.errorString === "DB Error") {
            connectionError.dispatch(errorSlice.actions.setTrue())
        }
        return data.dataJson
    } catch (error) {
        connectionError.dispatch(errorSlice.actions.setTrue())
    }
    return "-1";
}

export const addDiscountToStore = async (storeId: number, discountId: number): Promise<string> => {
    let request = {
        policyId1: storeId,
        policyId2: discountId
    }

    try {
        const response = await axios.post(`${server}/api/stores/addDiscountPolicyToStore?username=${localStorage.getItem("username")}`, request, {
            headers: {
                'Content-Type': 'application/json',
                Authorization: `Bearer ${localStorage.getItem("token")}` // Uncomment if you have a JWT token
            }
        }
        );
        let data: RestResponse = await response.data;
        if (data.error && data.errorString === "DB Error") {
            connectionError.dispatch(errorSlice.actions.setTrue())
        }
        return data.dataJson
    } catch (error) {
        connectionError.dispatch(errorSlice.actions.setTrue())
    }
    return "-1";
}


export const removeDiscountFromStore = async (storeId: number, discountId: number): Promise<string> => {
    let request = {
        policyId1: storeId,
        policyId2: discountId
    }
    try {
        const response = await axios.patch(`${server}/api/stores/removeDiscountPolicyToStore?username=${localStorage.getItem("username")}`, request, {
            headers: {
                'Content-Type': 'application/json',
                Authorization: `Bearer ${localStorage.getItem("token")}` // Uncomment if you have a JWT token
            }
        }
        );
        let data: RestResponse = await response.data;
        if (data.error && data.errorString === "DB Error") {
            connectionError.dispatch(errorSlice.actions.setTrue())
        }
        return data.dataJson
    } catch (error) {
        connectionError.dispatch(errorSlice.actions.setTrue())
    }
    return "-1";
}

export const changeProductAmountInCartGuest = async (productId: number, storeId: number, amount: number) => {
    try {
        const response = (await axios.patch(`${server}/api/user/guest/changeQuantityCart?guestId=${localStorage.getItem("guestId")}`, { storeId: storeId, productId: productId, amount: amount })).data;
        if (response.error && response.errorString === "DB Error") {
            connectionError.dispatch(errorSlice.actions.setTrue())
        }
        return response;
    } catch (error) {
        connectionError.dispatch(errorSlice.actions.setTrue())
    }
    return { dataJson: "this will never happen", error: true, errorString: "We couldn't reach the server" };
}

export const removeProductFromStore = async (productId: number, storeId: number) => {
    try {
        const response = (await axios.delete(`${server}/api/stores/deleteProductFromStore?username=${localStorage.getItem("username")}`, {
            headers: {
                'Content-Type': 'application/json',
                Authorization: `Bearer ${localStorage.getItem("token")}`
            },
            data: { storeId: storeId, productId: productId }
        }
        )).data;
        if (response.error && response.errorString === "DB Error") {
            connectionError.dispatch(errorSlice.actions.setTrue())
        }
        return response;
    } catch (error) {
        connectionError.dispatch(errorSlice.actions.setTrue())
    }
    return { dataJson: "this will never happen", error: true, errorString: "We couldn't reach the server" };
}


export const removeProductFromCart = async (productId: number, storeId: number) => {
    try {
        const response = (await axios.patch(`${server}/api/user/removeProductFromCart?username=${localStorage.getItem("username")}`, { storeId: storeId, productId: productId }, {
            headers: {
                'Content-Type': 'application/json',
                Authorization: `Bearer ${localStorage.getItem("token")}`
            }
        }
        )).data;
        if (response.error && response.errorString === "DB Error") {
            connectionError.dispatch(errorSlice.actions.setTrue())
        }
        return response;
    } catch (error) {
        connectionError.dispatch(errorSlice.actions.setTrue())
    }
    return { dataJson: "this will never happen", error: true, errorString: "We couldn't reach the server" };
}
export const removeProductFromCartGuest = async (productId: number, storeId: number) => {
    try {
        const response = (await axios.patch(`${server}/api/user/guest/removeProductFromCart?guestId=${localStorage.getItem("guestId")}`, { storeId: storeId, productId: productId })).data;
        if (response.error && response.errorString === "DB Error") {
            connectionError.dispatch(errorSlice.actions.setTrue())
        }
        return response;
    } catch (error) {
        connectionError.dispatch(errorSlice.actions.setTrue())
    }
    return { dataJson: "this will never happen", error: true, errorString: "We couldn't reach the server" };
}
export const checkCart = async (username: string) => {
    try {
        const response = (await axios.post(`${server}/api/user/checkMemberCart?username=${localStorage.getItem("username")}`, {}, {
            headers: {
                'Content-Type': 'application/json',
                Authorization: `Bearer ${localStorage.getItem("token")}`
            }
        }
        )).data;
        if (response.error && response.errorString === "DB Error") {
            connectionError.dispatch(errorSlice.actions.setTrue())
        }
        return response;
    } catch (error) {
        connectionError.dispatch(errorSlice.actions.setTrue())
    }
    return { dataJson: "this will never happen", error: true, errorString: "We couldn't reach the server" };
}


export const setStoreBankAccount = async (storeId: number, bank: BankAccountModel): Promise<RestResponse> => {
    try {
        const response = (await axios.post(`${server}/api/stores/setStoreBankAccount?username=${localStorage.getItem("username")}&storeId=${storeId}`, bank, {
            headers: {
                'Content-Type': 'application/json',
                Authorization: `Bearer ${localStorage.getItem("token")}` // Uncomment if you have a JWT token
            }
        }
        )).data;
        if (response.error && response.errorString === "DB Error") {
            connectionError.dispatch(errorSlice.actions.setTrue())
        }
        return response;
    } catch (error) {
        connectionError.dispatch(errorSlice.actions.setTrue())
    }
    return { dataJson: "this will never happen", error: true, errorString: "We couldn't reach the server" };
}

export const getStoreOrders = async (storeId: number, username: string): Promise<OrderModel[]> => {
    try {
        const response = await (await axios.get(`${server}/api/stores/getStoreOrders?username=${username}&storeId=${storeId}`, {
            headers: {
                'Content-Type': 'application/json',
                Authorization: `Bearer ${localStorage.getItem("token")}` // Uncomment if you have a JWT token
            }
        })).data;
        const ordersData: { [key: number]: ProductDataPrice[] } = JSON.parse(response.dataJson);
        const orders: OrderModel[] = Object.entries(ordersData).map(([orderId, products]) => {
            const total = products.reduce((acc, product) => acc + product.newPrice * product.amount, 0);
            const orderProducts: ProductOrderModel[] = products.map(product => ({
                id: product.id,
                name: product.name,
                quantity: product.amount,
                storeId: product.storeId,
                oldPrice: product.oldPrice,
                newPrice: product.newPrice
            }));
            const date = new Date().toLocaleDateString();
            return {
                id: orderId,
                date: date,
                total: total,
                products: orderProducts
            };
        });

        return orders;
    } catch (error) {
        console.error("Failed to fetch orders:", error);
        return [];
    }
}

export const getStoreByName = async (storeName: string, username: string | null): Promise<any> => {
    try {
        let url = `${server}/api/stores/getStoreByName?storeName=${storeName}`;
        if (username) {
            url += `&username=${username}`;
        }

        const response = (await axios.get(url, {
            headers: {
                'Content-Type': 'application/json',
                Authorization: `Bearer ${localStorage.getItem('token')}`
            }
        })).data;

        return response; // Adjust this based on the actual response structure
    } catch (error) {
        console.error("Failed to fetch store data:", error);
        return { dataJson: "this will never happen", error: true, errorString: "We couldn't reach the server" };;
    }
};


export const buyCart = async (purchase: PurchaseInfoModel): Promise<RestResponse> => {
    let request = {
        creditCard: {
            creditCardNumber: purchase.creditCard,
            digitsOnTheBack: purchase.digitsOnBack,
            expirationDate: purchase.expirationDate,
            ownerId: purchase.ownerId,
            ownerName: purchase.ordererName
        },
        addressDTO: {
            country: purchase.country,
            city: purchase.city,
            addressLine1: purchase.addressLine1,
            addressLine2: purchase.addressLine2,
            zipCode: purchase.zipCode,
            ordererName: purchase.ordererName,
            contactPhone: purchase.contactPhone,
            contactEmail: purchase.contactEmail,
        }
    }
    let url = `${server}/api/user/guest/purchaseCart?guestId=${localStorage.getItem("guestId")}`;
    let headers:any = {
        'Content-Type': 'application/json',
        //Authorization: `Bearer ${localStorage.getItem("token")}` // Uncomment if you have a JWT token
    }
    if (localStorage.getItem("token") != null) {
        url = `${server}/api/user/purchaseCart?username=${localStorage.getItem("username")}`
        headers = {
            'Content-Type': 'application/json',
            Authorization: `Bearer ${localStorage.getItem("token")}` // Uncomment if you have a JWT token
        }
    }
    try {
        const response = (await axios.post(url, request, {
            headers: headers
        }
        )).data;
        if (response.error && response.errorString === "DB Error") {
            connectionError.dispatch(errorSlice.actions.setTrue())
        }
        return response;
    } catch (error) {
        connectionError.dispatch(errorSlice.actions.setTrue())
    }
    return { dataJson: "this will never happen", error: true, errorString: "We couldn't reach the server" };
}

export const getTopProducts = async (): Promise<RestResponse> => {
    try {
        const res = await fetch(`${server}/api/product/getTopProducts`);
        const data: RestResponse = await res.json();
        if (data.error && data.errorString === "DB Error") {
            connectionError.dispatch(errorSlice.actions.setTrue())
        }
        return data;
    } catch (error) {
        connectionError.dispatch(errorSlice.actions.setTrue())
    }
    return { dataJson: "this will never happen", error: true, errorString: "We couldn't reach the server" };
}

export const exitGuest = async (guestId: number) => {
    try {
        const response = (await axios.post(`${server}/api/user/exitGuest`, null, {
            headers: {
                'Content-Type': 'application/json',
            },
            params: {
                guestId,
            },
        })).data;
        if (response.error && response.errorString === "DB Error") {
            connectionError.dispatch(errorSlice.actions.setTrue())
        }
        return response;
    } catch (error) {
        connectionError.dispatch(errorSlice.actions.setTrue())
    }
    return { dataJson: "this will never happen", error: true, errorString: "We couldn't reach the server" };;
};

export const isGuestExists = async (guestId: number) => {
    try {
        const response = (await axios.post(`${server}/api/user/checkGuestExist?guestId=${guestId}`, {})).data;
        if (response.error && response.errorString === "DB Error") {
            connectionError.dispatch(errorSlice.actions.setTrue())
        }
        return response;
    } catch (error) {
        connectionError.dispatch(errorSlice.actions.setTrue())
    }
    return { dataJson: "this will never happen", error: true, errorString: "We couldn't reach the server" };;
}

export const checkCartGuest = async (guestId: number) => {
    try {
        const response = (await axios.post(`${server}/api/user/checkGuestCart?guestId=${guestId}`, {})).data;
        if (response.error && response.errorString === "DB Error") {
            connectionError.dispatch(errorSlice.actions.setTrue())
        }
        return response;
    } catch (error) {
        connectionError.dispatch(errorSlice.actions.setTrue())
    }
    return { dataJson: "this will never happen", error: true, errorString: "We couldn't reach the server" };;
}