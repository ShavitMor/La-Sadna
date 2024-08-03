import React, { useEffect } from 'react';
import '../styles/CowComponent.css';
import cow from '../images/cow.png';
import gnome from '../images/gnome.gif'
import meme from '../images/meme.png'
import { checkAlive, setFalse } from '../API';
import { useDispatch, useSelector } from 'react-redux';

const CowComponent = () => {
  const conerror = useSelector((state:any) => state.conerror.value)
  const dispatch = useDispatch()
  const checkIfServerBack = async () => {
    let alive = await checkAlive();
    if(alive){
      dispatch(setFalse())
      if(window.location.href === window.location.origin+"/"){
        window.location.reload()
      }else{
        window.location.href = window.location.origin+"/";
      }
    }else{
      console.log("server still dead")
    }
  }
  useEffect(() => {
    const interval = setInterval(() => checkIfServerBack(), 30000);
    return () => {
      clearInterval(interval);
    };
  }, []);

  let sources = [
    {src: "https://media1.tenor.com/m/5jpxCVc4hHgAAAAC/cow-dancing.gif", class: "cow-animation", width:100},
    {src: cow, class: "cow-animation", width:100},
    {src: cow, class: "cow-animation", width:100},
    {src: "https://media1.tenor.com/m/5jpxCVc4hHgAAAAC/cow-dancing.gif", class: "cow-animation", width:100},
    {src: "https://media1.tenor.com/m/qBYH22fSZSAAAAAC/toilet-spin.gif", class: "cow-animation", width:150},
    {src: gnome, class: "stationary", width:150},
    {src: meme, class: "stationary", width:200}
  ]

  let randomSource = sources[Math.floor(Math.random() * sources.length)]

  return (
    <div className="cow-container">
      <div className={randomSource.class}>
        <img src={randomSource.src} alt="Cow" width={randomSource.width} />
      </div>
      <div className="message">
        We are having connection problems, please come back later...
      </div>
    </div>
  );
};

export default CowComponent;
