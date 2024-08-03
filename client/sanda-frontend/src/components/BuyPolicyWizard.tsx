import React, { createContext, useContext, useEffect, useState } from 'react';
import '../styles/Wizard.css';
import dayjs, { Dayjs } from 'dayjs';
import Store from './Store';
import Login from './Login';
import Profile from './Profile';
import { TimePicker } from '@mui/x-date-pickers/TimePicker';
import { AdapterDayjs } from '@mui/x-date-pickers/AdapterDayjs';
import Select, { SingleValue, ActionMeta } from 'react-select';



import { FormControl, FormControlLabel, FormLabel, Radio, RadioGroup, TextField } from '@mui/material';
import { useNavigate, useParams } from 'react-router-dom';
import { addPolicyToStore, createAgePolicy, createAmountPolicy, createCompositeCondition, createCompositePolicy, createDatePolicy, createHolidayPolicy, createHourPolicy, createKgPolicy, createMinAmountCondition, createMinBuyCondition, createMonthPolicy, describeBuyPolicy, describeCondition, describeDiscountPolicy, hasPermission, searchAndFilterStoreProducts } from '../API';
import Permission from '../models/Permission';
import RestResponse from '../models/RestResponse';
import { LocalizationProvider } from '@mui/x-date-pickers';
import { NumberInput } from './NumberInput';
import ProductModel from '../models/ProductModel';

const ProductsCategContext = createContext<{ products: any[], categs: any[] }>({
    products: [],
    categs: []
});

export const BuyPolicyWizard = () => {
    const [currentElement, setCurrentElement] = useState(<WeightPolicy />);
    const {storeId} = useParams();
    const [products, setProducts] = useState<any[]>([]);
    const [categs, setCategs] = useState<any[]>([]);
    const navigate = useNavigate();
    interface Dictionary<T> {
        [Key: string]: T;
    }

    useEffect(() => {
        const checkAllowed = async ()=> {
            let canAccess: boolean = await hasPermission(storeId!, Permission.ADD_BUY_POLICY);
            if(!canAccess){
                navigate('/permission-error', {state: "You do not have Edit Buy Policies permission in the given store"})
            }else {
                let allProducts: ProductModel[] = await searchAndFilterStoreProducts(storeId!, "", "", -1, -1);
                let categs: string[] = [];
                let categValues: any[] = [];
                let productValues: any[] = [];
                allProducts.forEach(product => {
                    if (!categs.includes(product.productCategory)) {
                        categs.push(product.productCategory);
                        categValues.push({ value: product.productCategory, label: product.productCategory })
                    }
                    productValues.push({ value: `${product.productID}`, label: `${product.productID} - ${product.productName}` })
                })
                setProducts(productValues);
                setCategs(categValues);
            }
        }
        checkAllowed();
    }, [])

    let textToElement: Dictionary<JSX.Element> = {}
    textToElement["Restrict Product Weight"] = <WeightPolicy />
    textToElement["Restrict Product Amount"] = <AmountPolicy />
    textToElement["Restrict User Age"] = <AgePolicy />
    textToElement["Restrict By Hour"] = <HourPolicy />
    textToElement["Restrict By Jewish Customs"] = <RoshKodeshPolicy />
    textToElement["Restrict On Holiday"] = <HolidayPolicy />
    textToElement["Restrict By Date"] = <DatePolicy />
    textToElement["Composite Policy"] = <CompositePolicy />

    return (
        <ProductsCategContext.Provider value={{ products: products, categs: categs }}>
        <div className="wizard">
            <div className="selector">
                {Object.keys(textToElement).map(buttontext => <button onClick={() => setCurrentElement(textToElement[buttontext])} className='selectorbutton'>{buttontext}</button>)}
            </div>
            <div className='element'>
                {currentElement}
            </div>
        </div>
        </ProductsCategContext.Provider>
    );
};

const WeightPolicy = () => {
    const [product, setProduct] = useState("");
    const [minWeight, setMin] = useState("-1");
    const [maxWeight, setMax] = useState("0");
    const {storeId} = useParams();
    const { products, categs } = useContext(ProductsCategContext);

    const onCreate = async () => {
        if(product === ""){
            alert("You must choose product");
            return;
        }
        let id: string = await createKgPolicy(parseInt(product), parseFloat(maxWeight), parseFloat(minWeight))
        alert(`Buy policy created with ID ${id}`)
    }

    const onCreateAndSave = async () => {
        if(product === ""){
            alert("You must choose product");
            return;
        }
        let id: string = await createKgPolicy(parseInt(product), parseFloat(maxWeight), parseFloat(minWeight))
        alert(`Buy policy created with ID ${id}`)
        await addPolicyToStore(parseInt(storeId!), parseInt(id));
    }

    return (
        <div className='discountEditor'>
            <h3>A certain product will only be allowed to be purchased within a range of weight</h3>
            <Select options={products} isSearchable={true} onChange={(option, action) => setProduct(option.value)} />
            <h1/>
            <NumberInput placeholder='Minimum Weight' onChange={(event, val) => setMin(`${val}`)} min={-1}/>
            <h1/>
            <NumberInput placeholder='Maximum Weight' onChange={(event, val) => setMax(`${val}`)} min={-1}/>
            <button onClick={onCreate} className='editorButton'>Create</button>
            <button onClick={onCreateAndSave} className='editorButton'>Create and add to store</button>
        </div>
    );
};

const AmountPolicy = () => {
    const [product, setProduct] = useState("");
    const [minAmount, setMin] = useState("-1");
    const [maxAmount, setMax] = useState("0");
    const {storeId} = useParams();
    const { products, categs } = useContext(ProductsCategContext);

    const onCreate = async () => {
        if(product === ""){
            alert("You must choose product");
            return;
        }
        let id: string = await createAmountPolicy(parseInt(product), parseInt(minAmount), parseInt(maxAmount))
        alert(`Buy policy created with ID ${id}`)
    }

    const onCreateAndSave = async () => {
        if(product === ""){
            alert("You must choose product");
            return;
        }
        let id: string = await createAmountPolicy(parseInt(product), parseInt(minAmount), parseInt(maxAmount))
        alert(`Buy policy created with ID ${id}`)
        await addPolicyToStore(parseInt(storeId!), parseInt(id));
    }

    return (
        <div className='discountEditor'>
            <h3>A certain product will only be allowed to be purchased within a range of amount</h3>
            <Select options={products} isSearchable={true} onChange={(option, action) => setProduct(option.value)} />
            <h1/>
            <NumberInput placeholder='Minimum Amount' onChange={(event, val) => setMin(`${val}`)} min={-1}/>
            <h1/>
            <NumberInput placeholder='Maximum Amount' onChange={(event, val) => setMax(`${val}`)} min={-1}/>
            <button onClick={onCreate} className='editorButton'>Create</button>
            <button onClick={onCreateAndSave} className='editorButton'>Create and add to store</button>
        </div>
    );
};

const AgePolicy = () => {
    const [category, setCategory] = useState("");
    const [minAge, setMin] = useState("");
    const [maxAge, setMax] = useState("");
    const {storeId} = useParams();
    const { products, categs } = useContext(ProductsCategContext);

    const onCreate = async () => {
        if(category === ""){
            alert("You must choose category");
            return;
        }
        let id: string = await createAgePolicy(category, parseInt(minAge), parseInt(maxAge))
        alert(`Buy policy created with ID ${id}`)
    }

    const onCreateAndSave = async () => {
        if(category === ""){
            alert("You must choose category");
            return;
        }
        let id: string = await createAgePolicy(category, parseInt(minAge), parseInt(maxAge))
        alert(`Buy policy created with ID ${id}`)
        await addPolicyToStore(parseInt(storeId!), parseInt(id));
    }

    return (
        <div className='discountEditor'>
            <h3>A certain category will only be allowed to be purchased within a range of the buyer's age</h3>
            <Select options={categs} isSearchable={true} onChange={(option, action) => setCategory(option.value)} />
            <h1/>
            <NumberInput placeholder='Minimum Age' onChange={(event, val) => setMin(`${val}`)} min={-1}/>
            <h1/>
            <NumberInput placeholder='Maximum Age' onChange={(event, val) => setMax(`${val}`)} min={-1}/>
            <button onClick={onCreate} className='editorButton'>Create</button>
            <button onClick={onCreateAndSave} className='editorButton'>Create and add to store</button>
        </div>
    );
};

const HourPolicy = () => {
    const [category, setCategory] = useState("");
    const [fromTime, setMin] = useState(dayjs('2024-04-17T12:00'));
    const [toTime, setMax] = useState(dayjs('2024-04-17T12:00'));
    const {storeId} = useParams();
    const { products, categs } = useContext(ProductsCategContext);

    const onCreate = async () => {
        if(category === ""){
            alert("You must choose category");
            return;
        }
        let id: string = await createHourPolicy(category, fromTime.hour(), fromTime.minute(), toTime.hour(), toTime.minute())
        alert(`Buy policy created with ID ${id}`)
    }

    const onCreateAndSave = async () => {
        if(category === ""){
            alert("You must choose category");
            return;
        }
        let id: string = await createHourPolicy(category, fromTime.hour(), fromTime.minute(), toTime.hour(), toTime.minute())
        alert(`Buy policy created with ID ${id}`)
        await addPolicyToStore(parseInt(storeId!), parseInt(id));
    }

    return (
        <div className='discountEditor'>
            <h3>A certain category will only be allowed to be purchased at a certain time of day</h3>
            <Select options={categs} isSearchable={true} onChange={(option, action) => setCategory(option.value)} />
            <h1/>
            <LocalizationProvider dateAdapter={AdapterDayjs}>
                <TimePicker label="From" value={fromTime} onChange={(newValue) => setMin(newValue!)}/>
            </LocalizationProvider>
            <h1/>
            <LocalizationProvider dateAdapter={AdapterDayjs}>
                <TimePicker label="To" value={toTime} onChange={(newValue) => setMax(newValue!)}/>
            </LocalizationProvider>
            <button onClick={onCreate} className='editorButton'>Create</button>
            <button onClick={onCreateAndSave} className='editorButton'>Create and add to store</button>
        </div>
    );
};

const RoshKodeshPolicy = () => {
    const [category, setCategory] = useState("");
    const {storeId} = useParams();
    const { products, categs } = useContext(ProductsCategContext);

    const onCreate = async () => {
        if(category === ""){
            alert("You must choose category");
            return;
        }
        let id: string = await createMonthPolicy(category)
        alert(`Buy policy created with ID ${id}`)
    }

    const onCreateAndSave = async () => {
        if(category === ""){
            alert("You must choose category");
            return;
        }
        let id: string = await createMonthPolicy(category)
        alert(`Buy policy created with ID ${id}`)
        await addPolicyToStore(parseInt(storeId!), parseInt(id));
    }

    return (
        <div className='discountEditor'>
            <h3>A certain category will not be sold at the start of the hebrew month</h3>
            <Select options={categs} isSearchable={true} onChange={(option, action) => setCategory(option.value)} />
            <button onClick={onCreate} className='editorButton'>Create</button>
            <button onClick={onCreateAndSave} className='editorButton'>Create and add to store</button>
        </div>
    );
};

const HolidayPolicy = () => {
    const [category, setCategory] = useState("");
    const {storeId} = useParams();
    const { products, categs } = useContext(ProductsCategContext);

    const onCreate = async () => {
        if(category === ""){
            alert("You must choose category");
            return;
        }
        let id: string = await createHolidayPolicy(category)
        alert(`Buy policy created with ID ${id}`)
    }

    const onCreateAndSave = async () => {
        if(category === ""){
            alert("You must choose category");
            return;
        }
        let id: string = await createHolidayPolicy(category)
        alert(`Buy policy created with ID ${id}`)
        await addPolicyToStore(parseInt(storeId!), parseInt(id));
    }

    return (
        <div className='discountEditor'>
            <h3>A certain category will not be sold during holidays</h3>
            <Select options={categs} isSearchable={true} onChange={(option, action) => setCategory(option.value)} />
            <button onClick={onCreate} className='editorButton'>Create</button>
            <button onClick={onCreateAndSave} className='editorButton'>Create and add to store</button>
        </div>
    );
};

const DatePolicy = () => {
    const [category, setCategory] = useState("");
    const [day, setDay] = useState("-1");
    const [month, setMonth] = useState("-1");
    const [year, setYear] = useState("-1");
    const {storeId} = useParams();
    const { products, categs } = useContext(ProductsCategContext);


    const onCreate = async () => {
        if(category === ""){
            alert("You must choose category");
            return;
        }
        let id: string = await createDatePolicy(category, parseInt(day), parseInt(month), parseInt(year))
        alert(`Buy policy created with ID ${id}`)
    }

    const onCreateAndSave = async () => {
        if(category === ""){
            alert("You must choose category");
            return;
        }
        let id: string = await createDatePolicy(category, parseInt(day), parseInt(month), parseInt(year))
        alert(`Buy policy created with ID ${id}`)
        await addPolicyToStore(parseInt(storeId!), parseInt(id));
    }

    return (
        <div className='discountEditor'>
            <h3>A certain category will not be sold during a specified day, month or year</h3>
            <Select options={categs} isSearchable={true} onChange={(option, action) => setCategory(option.value)} />
            <h1/>
            <NumberInput placeholder='Day' onChange={(event, val) => setDay(`${val}`)} min={-1} max={31}/>
            <h1/>
            <NumberInput placeholder='Month' onChange={(event, val) => setMonth(`${val}`)} min={-1} max={12}/>
            <h1/>
            <NumberInput placeholder='Year' onChange={(event, val) => setYear(`${val}`)} min={-1}/>
            <button onClick={onCreate} className='editorButton'>Create</button>
            <button onClick={onCreateAndSave} className='editorButton'>Create and add to store</button>
        </div>
    );
};

const CompositePolicy = () => {
    const [condId, setCondId] = useState("-1");
    const [logic, setLogic] = useState("OR");
    const [id1, setId1] = useState("0");
    const [id2, setId2] = useState("0");
    const [desc1, setDesc1] = useState("");
    const [desc2, setDesc2] = useState("");
    const {storeId} = useParams();

    const onCreate = async () => {
        let id: string = await createCompositePolicy(parseInt(id1), parseInt(id2), logic);
        alert(`Buy policy created with ID ${id}`)
    }

    const onCreateAndSave = async () => {
        let id: string = await createCompositePolicy(parseInt(id1), parseInt(id2), logic);
        alert(`Buy policy created with ID ${id}`)
        await addPolicyToStore(parseInt(storeId!), parseInt(id));
    }

    useEffect(() => {
        fetchDescriptions();
      },[id1,id2])
    const logicList = ["OR", "AND", "Conditioning"]
    const fetchDescriptions = async () =>{
        let resp1: RestResponse = await describeBuyPolicy(id1);
        let resp2: RestResponse = await describeBuyPolicy(id2);
        if(resp1.error){
            setDesc1(`Error: ${resp1.errorString}`)
        }else{
            setDesc1(resp1.dataJson)
        }
        if(resp2.error){
            setDesc2(`Error: ${resp2.errorString}`)
        }else{
            setDesc2(resp2.dataJson)
        }
    }

    return (
        <div className='discountEditor'>
            <h3>A composite discount applies a discount based on two existing discounts, based on various logical operators</h3>
            <FormControl>
                <FormLabel id="group-label">Combination logic:</FormLabel>
                <RadioGroup
                    aria-labelledby="group-label"
                    defaultValue={"OR"}

                    value={logic}
                    onChange={(e,v) => setLogic(v)}
                    name="radio-buttons-group"
                >
                    {logicList.map(logi => <FormControlLabel value={logi} control={<Radio />} label={logi} />)}
                </RadioGroup>
            </FormControl>
            <h1/>
            <TextField type='number' size='small' id="outlined-basic" label="ID of first discount" variant="outlined" value={id1} onChange={(event: React.ChangeEvent<HTMLInputElement>) => { setId1(event.target.value); }} />
            <p className='policyDescription'>Description: {desc1}</p>
            <TextField type='number' size='small' id="outlined-basic" label="ID of second discount" variant="outlined" value={id2} onChange={(event: React.ChangeEvent<HTMLInputElement>) => { setId2(event.target.value); }} />
            <p className='policyDescription'>Description: {desc2}</p>
            <button onClick={onCreate} className='editorButton'>Create</button>
            <button onClick={onCreateAndSave} className='editorButton'>Create and add to store</button>
        </div>
    );
};



export default BuyPolicyWizard;
