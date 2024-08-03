import React, { useEffect, useState } from 'react';
import StoreModel from '../models/StoreModel';
import { getStoreDiscounts, getStoreInfo, getStorePolicies, hasPermission, isManager, isOwner, removeDiscountFromStore, removePolicyFromStore, searchAndFilterStoreProducts } from '../API';
import { useNavigate, useParams } from 'react-router-dom';
import ProductModel from '../models/ProductModel';
import { Rating } from 'react-simple-star-rating'
import '../styles/Store.css';
import ProductInStore from './ProductInStore';
import ActionDropdown from './ActionDropdown';
import RestResponse from '../models/RestResponse';
import PolicyDescriptionModel from '../models/PolicyDescriptionModel';
import Permission from '../models/Permission';
import { TiDelete } from "react-icons/ti";
import { DNA, InfinitySpin } from 'react-loader-spinner';

export const Store = () => {
    const {storeId} = useParams();
    const [store, setStore] = useState<StoreModel>({storeName: "LOADING", storeId: -1, rank: -1, address: "LOADING", email: "LOADING", phoneNumber: "LOADING", founderUsername: "LOADING"});
    const [searchTerm, setSearchTerm] = useState('');
    const navigate = useNavigate();
    const [searchCategory, setSearchCategory] = useState('all');
    const [actualMax, setActMax] = useState(0);
  const [maxPrice, setMaxPrice] = useState(0); // Default price
  const [products, setProducts] = useState<ProductModel[]>([])
  const [isOwnerBool, setIsOwner] = useState(false)
    const [isManagerBool, setIsManager] = useState(false)
    const [buyPolicies, setBuyPolicies] = useState<PolicyDescriptionModel[]>([])
    const [discountPolicies, setDiscountPolicies] = useState<PolicyDescriptionModel[]>([])
    const [canRemoveDisPolicy, setRemoveDis] = useState(false)
    const [canRemoveBuyPolicy, setRemoveBuy] = useState(false)
    const [allCategs, setAllCategs] = useState<string[]>([])
    const [loading, setLoading] = useState(false);
    const [loadingBuy, setLoadingBuy] = useState(false);
    const [loadingDiscount, setLoadingDiscount] = useState(false);

  useEffect(()=>{
    loadStore();
  }, [])
  useEffect(() => {
    loadProducts()
  },[searchTerm,searchCategory,maxPrice])

  const loadProducts = async () => {
    setLoading(true);
    setProducts(await searchAndFilterStoreProducts(storeId!, searchCategory, searchTerm, 0, maxPrice))
    setLoading(false);
  }

  const loadStore = async () => {
    var storeResponse: RestResponse = await getStoreInfo(storeId!);
    console.log(storeResponse);
    if(!storeResponse.error){
      setStore(JSON.parse(storeResponse.dataJson))
    }else{
      navigate('/permission-error', {state: storeResponse.errorString})
      return;
    }
    let owner: boolean = await isOwner(storeId!)
    let manager: boolean = await isManager(storeId!)
    setIsOwner(owner);
    setIsManager(manager);
    setLoadingBuy(true);
    setLoadingDiscount(true);
    setDiscountPolicies(await getStoreDiscounts(storeId!))
    setBuyPolicies(await getStorePolicies(storeId!))
    setRemoveBuy(await hasPermission(storeId!, Permission.REMOVE_BUY_POLICY))
    setLoadingBuy(false);
    setRemoveDis(await hasPermission(storeId!, Permission.REMOVE_DISCOUNT_POLICY))
    setLoadingDiscount(false);
    let allProducts: ProductModel[] = await searchAndFilterStoreProducts(storeId!, "", "", -1, -1);
    let categs: string[] = []
    let max: number = 0;
    allProducts.forEach(product => {
      console.log(product)
      if(product.productPrice > max){
        max = product.productPrice;
      }
      if(!(product.productCategory in categs)){
        categs.push(product.productCategory)
      }
    })
    setAllCategs(categs);
    setActMax(Math.ceil(max));
    setMaxPrice(Math.ceil(max));
  }

  const handleDeletePolicy = async (policyId: number) => {
    setLoadingBuy(true);
    await removePolicyFromStore(parseInt(storeId!), policyId)
    setBuyPolicies(await getStorePolicies(storeId!))
    setLoadingBuy(false);
  }

  const handleDeleteDiscount = async (policyId: number) => {
    setLoadingDiscount(true);
    await removeDiscountFromStore(parseInt(storeId!), policyId)
    setDiscountPolicies(await getStoreDiscounts(storeId!))
    setLoadingDiscount(false);
  }

  const handleInputChange = (event:any) => {
    setSearchTerm(event.target.value);
  };

  const handleCategoryChange = (event:any) => {
    setSearchCategory(event.target.value);
  };
    const handleMaxPriceChange = (event:any) =>{
        setMaxPrice(event.target.value);
     };

    return (
        <div>
            <div className = "description">
            <h1 className = "description">Welcome to {store!.storeName}</h1>
            <Rating initialValue={store!.rank} readonly={true} size={20}/>
            <div className="details">
                <p className = "details">Founded by {store!.founderUsername}</p>
                <p className='dot'>·</p>
                <p className = "details">{store!.address}</p>
                <p className='dot'>·</p>
                <p className = "details">{store!.email}</p>
            </div>
            <div className="policies">
              <div className="listPolicies">
                <h4>Purchase Policies:</h4>
                {loadingBuy && <DNA width={100} />}
                {buyPolicies.map(policy => <div className='policDiv'><p>{policy.description}</p>
                {canRemoveBuyPolicy && <button className='removeButton' onClick = {() => handleDeletePolicy(policy.policyId)}><TiDelete /></button>}</div>)}
              </div>
              <div className="listPolicies">
                <h4>Discount Policies:</h4>
                {loadingDiscount && <DNA width={100} />}
                {discountPolicies.map(policy => <div className='policDiv'><p>{policy.description}</p>
                {canRemoveDisPolicy && <button className='removeButton' onClick={() => handleDeleteDiscount(policy.policyId)}><TiDelete /></button>}</div>)}
              </div>
            </div>
            <div className='dropdownDiv'>
            {(isOwnerBool || isManagerBool) &&
                <ActionDropdown storeId = {storeId}/>
            }
            </div>
            </div>
            <div className="products">
                <h1 className="products">Products:</h1>
                {loading && <InfinitySpin width='100' color='black'/>}
                <nav className="searcharea butinstore">
        <select className="category"  // Add this line
            value={searchCategory} onChange={handleCategoryChange}>
            <option value="all">All</option>
            {allCategs.map(categ => <option value={categ}>{categ}</option>)}
          </select>
        <input className='search-inputs'
          type="text"
          placeholder="Search..."
          value={searchTerm}
          onChange ={handleInputChange}
        />
        
          <div className="price-range">
            <label>
              Max Price: ${maxPrice}
              <input
                type="range"
                min={0}
                max={actualMax}
                value={maxPrice}
                onChange={handleMaxPriceChange}
              />
            </label>
          </div>        
    </nav>
                <div className='productsgrid'>
                    {products.map(product => 
                        <ProductInStore product = {product}/>
                    )}
                </div>
            </div>
        </div>
    );
};

export default Store;