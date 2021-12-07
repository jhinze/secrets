import React from 'react';
import {Box, Button, Tooltip} from "@mui/material";
import FileCopyIcon from "@material-ui/icons/FileCopy";

type CopyButtonProps = {
  text: string
  innerText?: string
  disableTip?: boolean
}

const initialState = {
  toolTipOpen: false,
  toolTip: "Copy"
}

export default function CopyButton(props: CopyButtonProps) {

  const [copyButtonState, setCopyButtonState] = React.useState(initialState)

  const handleClose = () => {
    setCopyButtonState({
      ...copyButtonState,
      ...{
        toolTipOpen: false
      }
    })
  };

  const handleOpen = () => {
    setCopyButtonState({
      ...copyButtonState,
      ...{
        toolTipOpen: true
      }
    })
  };

  const handleClick = () => {
    navigator.clipboard.writeText(props.text)
      .then(() => {
        setCopyButtonState({
          ...copyButtonState,
          ...{
            toolTip: "Copied!",
            toolTipOpen: true
          }
        })
        setTimeout(() => {
          setCopyButtonState({
            ...copyButtonState,
            ...{
              toolTip: "Copy",
            }
          })
        }, 2500);
      });
  }

  return (
    <Tooltip title={copyButtonState.toolTip}
             open={props.disableTip ? false : copyButtonState.toolTipOpen}
             onOpen={handleOpen}
             onClose={handleClose}>
      <Button id={"copy-button"} onClick={handleClick}>
        <FileCopyIcon/>
        {props.innerText &&
        <Box ml={1}>{props.innerText}</Box>
        }
      </Button>
    </Tooltip>
  );

}