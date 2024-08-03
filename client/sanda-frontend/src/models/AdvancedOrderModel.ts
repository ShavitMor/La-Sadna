import ProductDataPrice from "./ProductDataPrice";

export interface AdvancedOrderModel {
    id: number;
    memberName: string;
    storeId: number;
    storeNameWhenOrdered: string;
    productAmounts: ProductAmountsMap
    orderProductsJsons: ProductJSONMap
    products?: ProductDataPrice[]
}


interface ProductAmountsMap {
    [index: string]: number
}

interface ProductJSONMap {
    [index: string]: string
}